package newsmarthome.mqtt;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;

/**
 * Okresowo publikuje zmieniony stan urządzeń/czujników na MQTT. Działa na własnym,
 * dedykowanym schedulerze (nie na wspólnej puli używanej przez {@code Runners}, który
 * odpytuje hardware co ~2ms i nie może być blokowany publikacją sieciową).
 */
@Service
public class MqttStatePublisher {

    private static final long PUBLISH_INTERVAL_MS = 5000;

    private final Logger logger = LoggerFactory.getLogger(MqttStatePublisher.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, String> lastDeviceState = new ConcurrentHashMap<>();
    private final Map<Integer, String> lastDevicePosition = new ConcurrentHashMap<>();
    private final Map<Integer, String> lastSensorState = new ConcurrentHashMap<>();
    private ThreadPoolTaskScheduler scheduler;

    public MqttStatePublisher(MqttGateway gateway, SystemDAO systemDAO) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
    }

    @PostConstruct
    public void start() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("mqtt-state-publisher-");
        scheduler.initialize();
        // broker bez persystencji traci retained stany po restarcie - czyścimy cache, żeby
        // po reconnect wysłać pełny stan, a nie tylko to, co zmieniło się od ostatniej publikacji
        gateway.addConnectListener(() -> {
            lastDeviceState.clear();
            lastDevicePosition.clear();
            lastSensorState.clear();
        });
        scheduler.scheduleWithFixedDelay(this::publishChangedStates, PUBLISH_INTERVAL_MS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void publishChangedStates() {
        try {
            for (Device device : new ArrayList<>(systemDAO.getDevices())) {
                publishDeviceStateIfChanged(device);
            }
            for (Sensor sensor : new ArrayList<>(systemDAO.getSensors())) {
                publishSensorStateIfChanged(sensor);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas publikacji stanu MQTT: {}", e.getMessage(), e);
        }
    }

    private void publishDeviceStateIfChanged(Device device) {
        // pozycja przed stanem, żeby HA miał aktualną pozycję w chwili przyjścia stanu
        String position = MqttTopics.blindPositionPayload(device);
        if (position != null && !position.equals(lastDevicePosition.get(device.getId()))
                && gateway.publish(MqttTopics.devicePositionTopic(gateway.getBaseTopic(), device.getId()), position,
                        true)) {
            lastDevicePosition.put(device.getId(), position);
        }
        String payload = MqttTopics.deviceStatePayload(device);
        if (payload == null) {
            return;
        }
        String previous = lastDeviceState.get(device.getId());
        if (payload.equals(previous)) {
            return;
        }
        boolean published = gateway.publish(MqttTopics.deviceStateTopic(gateway.getBaseTopic(), device.getId()), payload,
                true);
        if (published) {
            lastDeviceState.put(device.getId(), payload);
        }
    }

    private void publishSensorStateIfChanged(Sensor sensor) {
        if (sensor.getTyp() != SensorsTypes.THERMOMETR && sensor.getTyp() != SensorsTypes.THERMOMETR_HYGROMETR) {
            return;
        }
        try {
            Map<String, Object> state = MqttTopics.sensorStatePayload(sensor);
            if (state == null) {
                return;
            }
            String payload = objectMapper.writeValueAsString(state);
            String previous = lastSensorState.get(sensor.getId());
            if (payload.equals(previous)) {
                return;
            }
            boolean published = gateway.publish(MqttTopics.sensorStateTopic(gateway.getBaseTopic(), sensor.getId()),
                    payload, true);
            if (published) {
                lastSensorState.put(sensor.getId(), payload);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas serializacji stanu czujnika id={}: {}", sensor.getId(), e.getMessage());
        }
    }
}
