package newsmarthome.mqtt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 *
 * <p>Identyfikator urządzenia HA zawiera id pokoju ({@link MqttTopics#haDeviceIdentifier}), bo HA
 * ustawia obszar z {@code suggested_area} tylko nowym urządzeniom - a urządzenie usunięte i dodane
 * z tym samym identyfikatorem przywraca ze starym obszarem. Zmiana identyfikatora (zmiana pokoju,
 * pierwszy start po wprowadzeniu tego schematu) wymaga usunięcia configu i ponownej publikacji po
 * {@link #recreateDelayMs} - inaczej HA zostawiłby encję przy starym urządzeniu.
 */
@Service
public class HaDiscoveryPublisher {

    private final Logger logger = LoggerFactory.getLogger(HaDiscoveryPublisher.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> pendingRemovals = ConcurrentHashMap.newKeySet();
    /** Topic configu -> identyfikator urządzenia HA, z którym go opublikowaliśmy. */
    private final Map<String, String> publishedIdentifiers = new ConcurrentHashMap<>();
    /**
     * Topic configu w trakcie odtwarzania -> chwila ({@link System#nanoTime()}), przed którą nie wolno
     * go opublikować. Dopóki wpis istnieje, publishJson pomija topic - także dla innych publikacji
     * (zmiana nazwy, publishAll), które inaczej wyprzedziłyby usunięcie urządzenia w HA.
     */
    private final Map<String, Long> recreating = new ConcurrentHashMap<>();
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

    /** Bez zapisu zmiana pokoju zrobiona przed restartem nie zostałaby wykryta. */
    @Value("${mqtt.published-identifiers-file:smarthome/database/mqtt_published_identifiers.txt}")
    private String publishedIdentifiersFile;

    /** Czas, jaki dajemy HA na usunięcie urządzenia z rejestru przed ponowną publikacją configu. */
    @Value("${mqtt.recreate-delay-ms:3000}")
    private long recreateDelayMs;

    public HaDiscoveryPublisher(MqttGateway gateway, SystemDAO systemDAO) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
    }

    @PostConstruct
    void loadState() {
        for (String line : readLines(pendingRemovalsFile)) {
            pendingRemovals.add(line);
        }
        for (String line : readLines(publishedIdentifiersFile)) {
            int separator = line.indexOf(' ');
            if (separator > 0) {
                publishedIdentifiers.put(line.substring(0, separator), line.substring(separator + 1));
            }
        }
    }

    /** Publikuje discovery dla wszystkich urządzeń i czujników znanych systemowi. */
    public synchronized void publishAll() {
        Set<String> cleared = new HashSet<>();
        for (String topic : pendingRemovals) {
            if (clearConfig(topic)) {
                // zaległe usunięcie mogło być częścią przeniesienia - odtwarzamy z opóźnieniem
                recreating.put(topic, notBeforeFromNow());
                cleared.add(topic);
            }
        }
        publishConfigs(null);
        if (!cleared.isEmpty()) {
            scheduleRecreate(cleared, () -> publishConfigs(cleared), "publikacji discovery po zaległych usunięciach");
        }
    }

    /** Publikuje configi wszystkich urządzeń i czujników, albo tylko tych, które mają topic w {@code topics}. */
    private synchronized void publishConfigs(Set<String> topics) {
        for (Device device : systemDAO.getDevicesSnapshot()) {
            String topic = deviceConfigTopic(device.getId(), device.getTyp());
            if (topics == null || topics.contains(topic)) {
                publishDevice(device);
            }
        }
        for (Sensor sensor : systemDAO.getSensorsSnapshot()) {
            if (topics == null || sensorConfigTopics(sensor.getId(), sensor.getTyp()).stream().anyMatch(topics::contains)) {
                publishSensor(sensor);
            }
        }
    }

    public synchronized void publishDevice(Device device) {
        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(device, gateway.getBaseTopic(),
                roomName(device.getRoom()));
        if (config == null) {
            return;
        }
        Map<String, Map<String, Object>> configs = new LinkedHashMap<>();
        configs.put(deviceConfigTopic(device.getId(), device.getTyp()), config);
        publishOrRecreate(configs, () -> {
            if (systemDAO.getDeviceByID(device.getId()) == device) {
                publishDevice(device);
            }
        }, "ponownej publikacji discovery urządzenia id=" + device.getId());
    }

    public synchronized void removeDevice(int deviceId, DeviceTypes typ) {
        String topic = deviceConfigTopic(deviceId, typ);
        if (topic != null) {
            remove(topic);
        }
    }

    public synchronized void publishSensor(Sensor sensor) {
        String component = MqttTopics.haComponentForSensor(sensor.getTyp());
        if (component == null) {
            return;
        }
        Map<String, Map<String, Object>> configs = new LinkedHashMap<>();
        for (Map<String, Object> config : MqttTopics.sensorDiscoveryConfigs(sensor, gateway.getBaseTopic(),
                roomName(sensor.getRoom()))) {
            configs.put(MqttTopics.discoveryConfigTopic(discoveryPrefix, component, (String) config.get("unique_id")),
                    config);
        }
        publishOrRecreate(configs, () -> {
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
            remove(topic);
        }
    }

    /**
     * Publikuje configi jednego urządzenia HA. Gdy zmienił się identyfikator urządzenia (np. przez
     * zmianę pokoju), najpierw usuwa configi i odtwarza je po {@link #recreateDelayMs}. Wszystkie
     * configi obiektu idą razem - encje higrometru dzielą jedno urządzenie HA.
     */
    private void publishOrRecreate(Map<String, Map<String, Object>> configs, Runnable republish, String description) {
        boolean identifierChanged = false;
        for (Map.Entry<String, Map<String, Object>> entry : configs.entrySet()) {
            String identifier = MqttTopics.haDeviceIdentifierOf(entry.getValue());
            // brak wpisu: config opublikowany przed wprowadzeniem identyfikatorów z pokojem (albo nowy obiekt)
            if (!identifier.equals(publishedIdentifiers.get(entry.getKey()))) {
                identifierChanged = true;
            }
        }
        if (!identifierChanged) {
            configs.forEach(this::publishJson);
            return;
        }
        // nowy identyfikator zapisujemy od razu - usunięcie starego configu gwarantują zaległe usunięcia
        // (przetrwają restart), a odtworzenie: recreate albo publishAll po reconnect
        configs.forEach((topic, config) -> publishedIdentifiers.put(topic, MqttTopics.haDeviceIdentifierOf(config)));
        savePublishedIdentifiers();
        for (String topic : configs.keySet()) {
            clearConfig(topic);
            recreating.put(topic, notBeforeFromNow());
        }
        scheduleRecreate(configs.keySet(), republish, description);
    }

    private void remove(String topic) {
        clearConfig(topic);
        if (publishedIdentifiers.remove(topic) != null) {
            savePublishedIdentifiers();
        }
    }

    private void scheduleRecreate(Collection<String> topics, Runnable republish, String description) {
        Set<String> copy = new HashSet<>(topics);
        schedule(() -> recreate(copy, republish, description), recreateDelayMs, description);
    }

    private synchronized void recreate(Set<String> topics, Runnable republish, String description) {
        for (String topic : topics) {
            // bez usunięcia na brokerze HA nie usunie starego urządzenia (encja zostałaby przy nim)
            if (pendingRemovals.contains(topic)) {
                if (!clearConfig(topic)) {
                    scheduleRecreate(topics, republish, description);
                    return;
                }
                recreating.put(topic, notBeforeFromNow());
            }
        }
        long now = System.nanoTime();
        long waitNanos = 0;
        for (String topic : topics) {
            Long notBefore = recreating.get(topic);
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
        recreating.keySet().removeAll(topics);
        republish.run();
    }

    private long notBeforeFromNow() {
        return System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(recreateDelayMs);
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
        writeLines(pendingRemovalsFile, new ArrayList<>(pendingRemovals));
    }

    private void savePublishedIdentifiers() {
        List<String> lines = new ArrayList<>();
        publishedIdentifiers.forEach((topic, identifier) -> lines.add(topic + " " + identifier));
        writeLines(publishedIdentifiersFile, lines);
    }

    private List<String> readLines(String file) {
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            return lines;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            logger.error("Nie udało się wczytać stanu discovery z {}: {}", file, e.getMessage());
        }
        return lines;
    }

    private void writeLines(String file, List<String> lines) {
        Path path = Paths.get(file);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Nie udało się zapisać stanu discovery do {}: {}", file, e.getMessage());
        }
    }

    private void publishJson(String topic, Map<String, Object> payload) {
        if (recreating.containsKey(topic)) {
            // HA jeszcze usuwa stare urządzenie - aktualny config opublikuje zaplanowany recreate
            logger.debug("Pominięto publikację {} - trwa odtwarzanie urządzenia w HA", topic);
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
