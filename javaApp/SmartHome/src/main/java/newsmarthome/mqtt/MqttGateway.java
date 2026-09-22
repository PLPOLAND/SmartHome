package newsmarthome.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private static final long INITIAL_CONNECT_RETRY_SECONDS = 10;

    private MqttClient client;
    private MqttConnectOptions options;
    private final Map<String, IMqttMessageListener> subscriptions = new ConcurrentHashMap<>();
    private final List<Runnable> connectListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mqtt-gateway");
        t.setDaemon(true);
        return t;
    });

    public String getBaseTopic() {
        return baseTopic;
    }

    /** Listener uruchamiany po każdym udanym (re)connect, poza wątkiem callbacków Paho. */
    public void addConnectListener(Runnable listener) {
        connectListeners.add(listener);
    }

    public synchronized void connect() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            options = new MqttConnectOptions();
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
                    // Blokujące wywołania poza wątkiem callbacków Paho: przy zapełnionej kolejce wiadomości
                    // przychodzących wątek odbiorczy czekałby na callback, a callback na ACK od odbiorczego.
                    executor.execute(() -> {
                        // Subskrypcje trzeba odtworzyć zanim ogłosimy "online" - inaczej HA może wysłać komendę
                        // zanim broker zdąży ją do nas dostarczyć (okno na utratę komendy po reconnect).
                        resubscribeAll();
                        publish(availabilityTopic, "online", true);
                        // Discovery (retained) może nie dotrzeć do brokera, jeśli był niedostępny przy starcie,
                        // albo zostać wyczyszczony po restarcie brokera bez persystencji - republikujemy go zawsze.
                        connectListeners.forEach(MqttGateway.this::runListener);
                    });
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
        } catch (MqttException e) {
            logger.error("Nie udało się utworzyć klienta MQTT ({}): {}", brokerUrl, e.getMessage());
            return;
        }
        tryInitialConnect();
    }

    // automaticReconnect w Paho działa dopiero po pierwszym udanym połączeniu, więc pierwszą próbę
    // ponawiamy sami, dopóki broker nie stanie się dostępny.
    private void tryInitialConnect() {
        try {
            client.connect(options);
        } catch (MqttException e) {
            logger.error("Nie udało się połączyć z brokerem MQTT ({}): {}. Ponowna próba za {}s", brokerUrl,
                    e.getMessage(), INITIAL_CONNECT_RETRY_SECONDS);
            executor.schedule(this::tryInitialConnect, INITIAL_CONNECT_RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void runListener(Runnable listener) {
        try {
            listener.run();
        } catch (RuntimeException e) {
            logger.error("Błąd w listenerze połączenia MQTT: {}", e.getMessage(), e);
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
        executor.shutdownNow();
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
            // gdy niepołączony, subskrypcję odtworzy resubscribeAll() w connectComplete
            if (client != null && client.isConnected()) {
                client.subscribe(topicFilter, 1, listener);
            }
        } catch (MqttException e) {
            logger.error("Błąd podczas subskrypcji MQTT na {}: {}", topicFilter, e.getMessage());
        }
    }
}
