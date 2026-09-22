package newsmarthome.mqtt;

import java.util.List;
import java.util.Map;

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

    @Value("${mqtt.discovery-prefix}")
    private String discoveryPrefix;

    public HaDiscoveryPublisher(MqttGateway gateway, SystemDAO systemDAO) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
    }

    /** Publikuje discovery dla wszystkich urządzeń i czujników znanych systemowi. */
    public void publishAll() {
        for (Device device : systemDAO.getDevices()) {
            publishDevice(device);
        }
        for (Sensor sensor : systemDAO.getSensors()) {
            publishSensor(sensor);
        }
    }

    public void publishDevice(Device device) {
        Map<String, Object> config = MqttTopics.deviceDiscoveryConfig(device, gateway.getBaseTopic());
        if (config == null) {
            return;
        }
        String component = MqttTopics.haComponentForDevice(device.getTyp());
        String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, component, MqttTopics.deviceObjectId(device.getId()));
        publishJson(topic, config);
    }

    public void removeDevice(int deviceId, DeviceTypes typ) {
        String component = MqttTopics.haComponentForDevice(typ);
        if (component == null) {
            return;
        }
        String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, component, MqttTopics.deviceObjectId(deviceId));
        gateway.publish(topic, "", true);
    }

    public void publishSensor(Sensor sensor) {
        List<Map<String, Object>> configs = MqttTopics.sensorDiscoveryConfigs(sensor, gateway.getBaseTopic());
        for (Map<String, Object> config : configs) {
            String objectId = (String) config.get("unique_id");
            String topic = MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, objectId);
            publishJson(topic, config);
        }
    }

    public void removeSensor(int sensorId, SensorsTypes typ) {
        if (typ != SensorsTypes.THERMOMETR && typ != SensorsTypes.THERMOMETR_HYGROMETR) {
            return;
        }
        gateway.publish(
                MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, MqttTopics.sensorObjectId(sensorId)),
                "", true);
        if (typ == SensorsTypes.THERMOMETR_HYGROMETR) {
            gateway.publish(
                    MqttTopics.discoveryConfigTopic(discoveryPrefix, SENSOR_COMPONENT, MqttTopics.humidityObjectId(sensorId)),
                    "", true);
        }
    }

    private void publishJson(String topic, Map<String, Object> payload) {
        try {
            gateway.publish(topic, objectMapper.writeValueAsString(payload), true);
        } catch (Exception e) {
            logger.error("Błąd podczas serializacji configu discovery dla {}: {}", topic, e.getMessage());
        }
    }
}
