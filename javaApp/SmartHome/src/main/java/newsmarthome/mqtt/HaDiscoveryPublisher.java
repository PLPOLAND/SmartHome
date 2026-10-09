package newsmarthome.mqtt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;

/**
 * Publikuje (i usuwa) konfiguracje Home Assistant MQTT Discovery dla urządzeń i czujników.
 */
@Service
public class HaDiscoveryPublisher {

    private final Logger logger = LoggerFactory.getLogger(HaDiscoveryPublisher.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> pendingRemovals = ConcurrentHashMap.newKeySet();
    /**
     * Topic configu usuniętego przy przeniesieniu -> chwila ({@link System#nanoTime()}), przed którą
     * nie wolno go opublikować. Pilnuje opóźnienia także przed innymi publikacjami (zmiana nazwy,
     * publishAll), które inaczej dopięłyby encję do starego urządzenia.
     */
    private final Map<String, Long> recreateNotBefore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService asyncExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ha-discovery");
        t.setDaemon(true);
        return t;
    });

    @Value("${mqtt.discovery-prefix}")
    private String discoveryPrefix;

    /** Zaległe usunięcia muszą przetrwać restart - inaczej retained config usuniętej encji zostałby w HA. */
    @Value("${mqtt.pending-removals-file:smarthome/database/mqtt_pending_removals.txt}")
    private String pendingRemovalsFile;

    /**
     * Odstęp między usunięciem a ponowną publikacją configu przy zmianie pokoju. HA musi zdążyć
     * usunąć urządzenie z rejestru, inaczej nowy config dopiąłby encję do starego urządzenia
     * (ze starym obszarem).
     */
    @Value("${mqtt.recreate-delay-ms:3000}")
    private long recreateDelayMs;

    public HaDiscoveryPublisher(MqttGateway gateway, SystemDAO systemDAO) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
    }

    @PostConstruct
    void loadPendingRemovals() {
        Path path = Paths.get(pendingRemovalsFile);
        if (!Files.exists(path)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    pendingRemovals.add(line.trim());
                }
            }
        } catch (IOException e) {
            logger.error("Nie udało się wczytać zaległych usunięć discovery z {}: {}", pendingRemovalsFile, e.getMessage());
        }
    }

    /** Publikuje discovery dla wszystkich urządzeń i czujników znanych systemowi. */
    public synchronized void publishAll() {
        List<String> cleared = new ArrayList<>();
        for (String topic : pendingRemovals) {
            if (clearForRecreate(topic)) {
                cleared.add(topic);
            }
        }
        // zaległe usunięcie mogło być przeniesieniem do innego pokoju - te configi publishJson
        // pominie i opublikujemy je dopiero po opóźnieniu, pozostałe idą od razu
        publishAllConfigs();
        if (!cleared.isEmpty()) {
            scheduleRecreate(cleared, this::publishAllConfigs, "publikacji discovery po zaległych usunięciach");
        }
    }

    private synchronized void publishAllConfigs() {
        for (Device device : systemDAO.getDevicesSnapshot()) {
            publishDevice(device);
        }
        for (Sensor sensor : systemDAO.getSensorsSnapshot()) {
            publishSensor(sensor);
        }
    }

    public synchronized void publishDevice(Device device) {
        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(device, gateway.getBaseTopic(),
                roomName(device.getRoom()));
        if (config == null) {
            return;
        }
        publishJson(deviceConfigTopic(device.getId(), device.getTyp()), config);
    }

    public synchronized void removeDevice(int deviceId, DeviceTypes typ) {
        String topic = deviceConfigTopic(deviceId, typ);
        if (topic != null) {
            clearConfig(topic);
        }
    }

    /**
     * Przenosi urządzenie do obszaru jego aktualnego pokoju. {@code suggested_area} działa w HA
     * tylko przy tworzeniu urządzenia, więc usuwamy je z HA i po {@link #recreateDelayMs}
     * publikujemy na nowo. HA traci przy tym zmiany zrobione w samym HA (nazwy, entity_id).
     */
    public synchronized void moveDevice(Device device) {
        String topic = deviceConfigTopic(device.getId(), device.getTyp());
        if (topic == null) {
            return;
        }
        List<String> topics = Collections.singletonList(topic);
        moveTopics(topics, () -> {
            if (systemDAO.getDeviceByID(device.getId()) == device) {
                publishDevice(device);
            }
        }, "ponownej publikacji discovery urządzenia id=" + device.getId());
    }

    public synchronized void publishSensor(Sensor sensor) {
        String component = MqttTopics.haComponentForSensor(sensor.getTyp());
        if (component == null) {
            return;
        }
        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(sensor, gateway.getBaseTopic(),
                roomName(sensor.getRoom()));
        for (Map<String, Object> config : configs) {
            String objectId = (String) config.get("unique_id");
            String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, component, objectId);
            publishJson(topic, config);
        }
    }

    /** Jak {@link #moveDevice(Device)}, dla czujników i przycisków. */
    public synchronized void moveSensor(Sensor sensor) {
        List<String> topics = sensorConfigTopics(sensor.getId(), sensor.getTyp());
        if (topics.isEmpty()) {
            return;
        }
        moveTopics(topics, () -> {
            if (systemDAO.getSensor(sensor.getId()) == sensor) {
                publishSensor(sensor);
            }
        }, "ponownej publikacji discovery czujnika id=" + sensor.getId());
    }

    /**
     * Jak {@link #publishSensor(Sensor)}, ale na własnym wątku - dla wywołań z wątków sprzętowych
     * ({@code Runners}), których nie wolno blokować publikacją sieciową (timeout do 10 s).
     */
    public void publishSensorAsync(Sensor sensor) {
        schedule(() -> publishSensor(sensor), 0, "publikacji discovery czujnika id=" + sensor.getId());
    }

    @PreDestroy
    public void stop() {
        asyncExecutor.shutdownNow();
    }

    public synchronized void removeSensor(int sensorId, SensorsTypes typ) {
        for (String topic : sensorConfigTopics(sensorId, typ)) {
            clearConfig(topic);
        }
    }

    /** Topic configu discovery urządzenia, lub null dla typów nieobsługiwanych przez HA. */
    private String deviceConfigTopic(int deviceId, DeviceTypes typ) {
        String component = MqttTopics.haComponentForDevice(typ);
        if (component == null) {
            return null;
        }
        return MqttTopics.discoveryConfigTopic(discoveryPrefix, component, MqttTopics.deviceObjectId(deviceId));
    }

    private List<String> sensorConfigTopics(int sensorId, SensorsTypes typ) {
        List<String> topics = new ArrayList<>();
        String component = MqttTopics.haComponentForSensor(typ);
        if (component != null) {
            for (String objectId : MqttTopics.sensorObjectIds(sensorId, typ)) {
                topics.add(MqttTopics.discoveryConfigTopic(discoveryPrefix, component, objectId));
            }
        }
        return topics;
    }

    // Ponowną publikację planujemy także, gdy usunięcie nie doszło do brokera - recreate ponowi
    // usunięcie, inaczej po częściowym błędzie (np. timeout) encja zniknęłaby z HA aż do restartu.
    private void moveTopics(List<String> topics, Runnable republish, String description) {
        for (String topic : topics) {
            clearForRecreate(topic);
        }
        scheduleRecreate(topics, republish, description);
    }

    /** Czyści config i otwiera okno, w którym publishJson go nie opublikuje. */
    private boolean clearForRecreate(String topic) {
        boolean published = clearConfig(topic);
        if (published) {
            recreateNotBefore.put(topic, System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(recreateDelayMs));
        }
        return published;
    }

    private void scheduleRecreate(List<String> topics, Runnable republish, String description) {
        schedule(() -> recreate(topics, republish, description), recreateDelayMs, description);
    }

    private synchronized void recreate(List<String> topics, Runnable republish, String description) {
        for (String topic : topics) {
            // bez usunięcia na brokerze HA nie utworzy urządzenia od nowa (zostałby stary obszar)
            if (pendingRemovals.contains(topic) && !clearForRecreate(topic)) {
                // broker niedostępny - publishAll po reconnect dokończy przeniesienie
                return;
            }
        }
        long now = System.nanoTime();
        long waitNanos = 0;
        for (String topic : topics) {
            Long notBefore = recreateNotBefore.get(topic);
            if (notBefore != null) {
                waitNanos = Math.max(waitNanos, notBefore - now);
            }
        }
        if (waitNanos > 0) {
            // usunięcie ponowione przed chwilą (tu albo w publishAll) - czekamy na nowe okno
            schedule(() -> recreate(topics, republish, description), TimeUnit.NANOSECONDS.toMillis(waitNanos) + 1,
                    description);
            return;
        }
        recreateNotBefore.keySet().removeAll(topics);
        republish.run();
    }

    private void schedule(Runnable task, long delayMs, String description) {
        try {
            asyncExecutor.schedule(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    logger.error("Błąd podczas {}: {}", description, e.getMessage(), e);
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            logger.warn("Pominięto zadanie {} - publisher discovery jest zatrzymany", description);
        }
    }

    private String roomName(int roomId) {
        Room room = systemDAO.getRoom(roomId);
        return room != null ? room.getName() : null;
    }

    // Usunięcie przy niedostępnym brokerze ponawiamy przy następnym publishAll (po reconnect),
    // inaczej retained config zostałby na brokerze i HA pokazywałby nieistniejącą encję.
    private boolean clearConfig(String topic) {
        boolean published = gateway.publish(topic, "", true);
        boolean changed = published ? pendingRemovals.remove(topic) : pendingRemovals.add(topic);
        if (changed) {
            savePendingRemovals();
        }
        return published;
    }

    private void savePendingRemovals() {
        Path path = Paths.get(pendingRemovalsFile);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, new ArrayList<>(pendingRemovals), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Nie udało się zapisać zaległych usunięć discovery do {}: {}", pendingRemovalsFile, e.getMessage());
        }
    }

    private void publishJson(String topic, Map<String, Object> payload) {
        Long notBefore = recreateNotBefore.get(topic);
        if (notBefore != null && notBefore - System.nanoTime() > 0) {
            // HA jeszcze usuwa urządzenie - aktualny config opublikuje zaplanowany recreate
            logger.debug("Pominięto publikację {} - trwa przenoszenie do innego obszaru", topic);
            return;
        }
        try {
            // zaległe usunięcie zdejmujemy dopiero po udanej publikacji - inaczej po restarcie
            // retained pusty config z brokera nie miałby już śladu do ponowienia
            boolean published = gateway.publish(topic, objectMapper.writeValueAsString(payload), true);
            if (published && pendingRemovals.remove(topic)) {
                savePendingRemovals();
            }
        } catch (Exception e) {
            logger.error("Błąd podczas serializacji configu discovery dla {}: {}", topic, e.getMessage());
        }
    }
}
