package newsmarthome.mqtt;

import java.nio.charset.StandardCharsets;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cienka warstwa nad klientem Paho MQTT: połączenie z brokerem (z LWT), publikacja i
 * subskrypcja. Logikę Home Assistant Discovery/stanów budują {@link HaDiscoveryPublisher},
 * {@link MqttStatePublisher} i {@link MqttCommandHandler}.
 */
@Component
public class MqttGateway {

    private final Logger logger = LoggerFactory.getLogger(MqttGateway.class);

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.base-topic}")
    private String baseTopic;

    private MqttClient client;

    public String getBaseTopic() {
        return baseTopic;
    }

    public synchronized void connect() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            String availabilityTopic = MqttTopics.availabilityTopic(baseTopic);
            options.setWill(availabilityTopic, "offline".getBytes(StandardCharsets.UTF_8), 1, true);
            client.connect(options);
            publish(availabilityTopic, "online", true);
            logger.info("Połączono z brokerem MQTT: {}", brokerUrl);
        } catch (MqttException e) {
            logger.error("Nie udało się połączyć z brokerem MQTT ({}): {}", brokerUrl, e.getMessage());
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                publish(MqttTopics.availabilityTopic(baseTopic), "offline", true);
                client.disconnect();
            }
        } catch (MqttException e) {
            logger.warn("Błąd podczas rozłączania z brokerem MQTT: {}", e.getMessage());
        }
    }

    public void publish(String topic, String payload, boolean retained) {
        if (client == null || !client.isConnected()) {
            logger.debug("MQTT niepołączony, pomijam publikację na {}", topic);
            return;
        }
        try {
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(retained);
            client.publish(topic, message);
        } catch (MqttException e) {
            logger.error("Błąd podczas publikacji MQTT na {}: {}", topic, e.getMessage());
        }
    }

    public void subscribe(String topicFilter, IMqttMessageListener listener) {
        try {
            if (client != null) {
                client.subscribe(topicFilter, 1, listener);
            }
        } catch (MqttException e) {
            logger.error("Błąd podczas subskrypcji MQTT na {}: {}", topicFilter, e.getMessage());
        }
    }
}
