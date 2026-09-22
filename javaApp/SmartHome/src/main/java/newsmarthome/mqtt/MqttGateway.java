package newsmarthome.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
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
    private final Map<String, IMqttMessageListener> subscriptions = new ConcurrentHashMap<>();

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
            // MqttCallbackExtended.connectComplete odpala się zarówno po pierwszym połączeniu, jak i po
            // każdym automatycznym wznowieniu (automaticReconnect) — dzięki temu re-publikujemy "online"
            // i odtwarzamy subskrypcje utracone przez cleanSession=true bez ręcznego zarządzania reconnectem.
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    logger.info(reconnect ? "Odzyskano połączenie z brokerem MQTT: {}" : "Połączono z brokerem MQTT: {}",
                            serverURI);
                    publish(availabilityTopic, "online", true);
                    resubscribeAll();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("Utracono połączenie z brokerem MQTT: {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // obsługiwane przez listenery przekazane do subscribe()
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // brak akcji - publikacje nie są śledzone
                }
            });
            client.connect(options);
        } catch (MqttException e) {
            logger.error("Nie udało się połączyć z brokerem MQTT ({}): {}", brokerUrl, e.getMessage());
        }
    }

    private void resubscribeAll() {
        subscriptions.forEach((topicFilter, listener) -> {
            try {
                client.subscribe(topicFilter, 1, listener);
            } catch (MqttException e) {
                logger.error("Błąd podczas ponownej subskrypcji MQTT na {}: {}", topicFilter, e.getMessage());
            }
        });
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

    /**
     * @return {@code true} jeśli wiadomość została faktycznie wysłana do brokera. Wywołujący, którzy
     *         cache'ują ostatnio wysłany stan (np. {@link MqttStatePublisher}), powinni aktualizować
     *         cache tylko gdy zwrócone {@code true} — inaczej zmiana stanu w trakcie rozłączenia
     *         zostanie bezpowrotnie utracona (nie zostanie ponowiona po odzyskaniu połączenia).
     */
    public boolean publish(String topic, String payload, boolean retained) {
        if (client == null || !client.isConnected()) {
            logger.debug("MQTT niepołączony, pomijam publikację na {}", topic);
            return false;
        }
        try {
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(retained);
            client.publish(topic, message);
            return true;
        } catch (MqttException e) {
            logger.error("Błąd podczas publikacji MQTT na {}: {}", topic, e.getMessage());
            return false;
        }
    }

    public void subscribe(String topicFilter, IMqttMessageListener listener) {
        subscriptions.put(topicFilter, listener);
        try {
            if (client != null) {
                client.subscribe(topicFilter, 1, listener);
            }
        } catch (MqttException e) {
            logger.error("Błąd podczas subskrypcji MQTT na {}: {}", topicFilter, e.getMessage());
        }
    }
}
