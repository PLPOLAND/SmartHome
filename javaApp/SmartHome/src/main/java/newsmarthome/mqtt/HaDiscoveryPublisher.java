package newsmarthome.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;

/**
 * Publikuje (i usuwa) konfiguracje Home Assistant MQTT Discovery dla urządzeń i czujników.
 */
@Service
public class HaDiscoveryPublisher {

    private static final String SENSOR_COMPONENT = "sensor";

    private final Logger logger = LoggerFactory.getLogger(HaDiscoveryPublisher.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> pendingRemovals = ConcurrentHashMap.newKeySet();

    @Value("${mqtt.discovery-prefix}")
    private String discoveryPrefix;

    public HaDiscoveryPublisher(MqttGateway gateway, SystemDAO systemDAO) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
    }

    /** Publikuje discovery dla wszystkich urządzeń i czujników znanych systemowi. */
    public synchronized void publishAll() {
        for (String topic : pendingRemovals) {
            clearConfig(topic);
        }
        for (Device device : new ArrayList<>(systemDAO.getDevices())) {
            publishDevice(device);
        }
        for (Sensor sensor : new ArrayList<>(systemDAO.getSensors())) {
            publishSensor(sensor);
        }
    }

    public synchronized void publishDevice(Device device) {
        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(device, gateway.getBaseTopic());
        if (config == null) {
            return;
        }
        String component = MqttTopics.haComponentForDevice(device.getTyp());
        String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, component, MqttTopics.deviceObjectId(device.getId()));
        publishJson(topic, config);
    }

    public synchronized void removeDevice(int deviceId, DeviceTypes typ) {
        String component = MqttTopics.haComponentForDevice(typ);
        if (component == null) {
            return;
        }
        clearConfig(MqttTopics.discoveryConfigTopic(discoveryPrefix, component, MqttTopics.deviceObjectId(deviceId)));
    }

    public synchronized void publishSensor(Sensor sensor) {
        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(sensor, gateway.getBaseTopic());
        for (Map<String, Object> config : configs) {
            String objectId = (String) config.get("unique_id");
            String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, objectId);
            publishJson(topic, config);
        }
    }

    public synchronized void removeSensor(int sensorId, SensorsTypes typ) {
        if (typ != SensorsTypes.THERMOMETR && typ != SensorsTypes.THERMOMETR_HYGROMETR) {
            return;
        }
        clearConfig(MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, MqttTopics.sensorObjectId(sensorId)));
        if (typ == SensorsTypes.THERMOMETR_HYGROMETR) {
            clearConfig(
                    MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, MqttTopics.humidityObjectId(sensorId)));
        }
    }

    // Usunięcie przy niedostępnym brokerze ponawiamy przy następnym publishAll (po reconnect),
    // inaczej retained config zostałby na brokerze i HA pokazywałby nieistniejącą encję.
    private void clearConfig(String topic) {
        if (gateway.publish(topic, "", true)) {
            pendingRemovals.remove(topic);
        } else {
            pendingRemovals.add(topic);
        }
    }

    private void publishJson(String topic, Map<String, Object> payload) {
        try {
            pendingRemovals.remove(topic);
            gateway.publish(topic, objectMapper.writeValueAsString(payload), true);
        } catch (Exception e) {
            logger.error("Błąd podczas serializacji configu discovery dla {}: {}", topic, e.getMessage());
        }
    }
}
