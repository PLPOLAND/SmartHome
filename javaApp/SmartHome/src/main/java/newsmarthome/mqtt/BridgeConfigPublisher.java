package newsmarthome.mqtt;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
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
import newsmarthome.i2c.MasterToSlaveConverter;

/**
 * Publikuje retained snapshot konfiguracji ({@link MqttTopics#bridgeConfigTopic}) - z niego
 * integracja Home Assistant buduje formularze (listy pokoi, urządzeń, wykrytych slave'ów).
 * Publikacja idzie na własnym wątku: wywołują ją m.in. wątki sprzętowe, których nie wolno
 * blokować siecią. Okresowe sprawdzenie wyłapuje zmiany bez jawnego zgłoszenia (np. podłączenie slave'a).
 */
@Service
public class BridgeConfigPublisher {

    private static final long REFRESH_INTERVAL_SECONDS = 30;
    private static final long RETRY_DELAY_SECONDS = 1;

    private final Logger logger = LoggerFactory.getLogger(BridgeConfigPublisher.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final MasterToSlaveConverter slaveSender;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mqtt-bridge-config");
        t.setDaemon(true);
        return t;
    });
    private String lastPublished;

    @Value("${mqtt.config.max-click-functions:3}")
    private int maxClickFunctions;

    public BridgeConfigPublisher(MqttGateway gateway, SystemDAO systemDAO, MasterToSlaveConverter slaveSender) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
        this.slaveSender = slaveSender;
    }

    @PostConstruct
    void start() {
        executor.scheduleWithFixedDelay(() -> publishIfChanged(false), REFRESH_INTERVAL_SECONDS,
                REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

    public int getMaxClickFunctions() {
        return maxClickFunctions;
    }

    /** Zgłasza zmianę konfiguracji - snapshot zostanie opublikowany poza wątkiem wywołującym. */
    public void requestPublish() {
        submit(false);
    }

    /** Broker bez persystencji traci retained po restarcie - po każdym connect publikujemy bezwarunkowo. */
    public void publishAfterConnect() {
        submit(true);
    }

    private void submit(boolean force) {
        try {
            executor.execute(() -> publishIfChanged(force));
        } catch (RejectedExecutionException e) {
            logger.debug("Pominięto publikację snapshotu konfiguracji - publisher jest zatrzymany");
        }
    }

    String buildPayload() throws Exception {
        Map<Integer, Boolean> slaves = new LinkedHashMap<>();
        for (Integer address : new TreeSet<>(slaveSender.getSlavesAdresses())) {
            slaves.put(address, slaveSender.isSlaveConnected(address));
        }
        String version = getClass().getPackage().getImplementationVersion();
        return objectMapper.writeValueAsString(MqttTopics.bridgeConfigPayload(version != null ? version : "dev",
                maxClickFunctions, slaves, systemDAO.getRoomsArrayList(), systemDAO.getDevicesSnapshot(),
                systemDAO.getSensorsSnapshot()));
    }

    private void publishIfChanged(boolean force) {
        publishIfChanged(force, true);
    }

    private void publishIfChanged(boolean force, boolean retryOnError) {
        try {
            String payload = buildPayload();
            if (!force && payload.equals(lastPublished)) {
                return;
            }
            if (gateway.publish(MqttTopics.bridgeConfigTopic(gateway.getBaseTopic()), payload, true)) {
                lastPublished = payload;
            }
        } catch (Exception e) {
            // np. równoległa zmiana listy pokoi w trakcie budowania - ponawiamy raz zamiast czekać na cykl
            logger.error("Błąd podczas publikacji snapshotu konfiguracji: {}", e.getMessage(), e);
            if (retryOnError) {
                try {
                    executor.schedule(() -> publishIfChanged(force, false), RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                } catch (RejectedExecutionException rejected) {
                    logger.debug("Pominięto ponowienie publikacji snapshotu - publisher jest zatrzymany");
                }
            }
        }
    }
}
