package newsmarthome.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;

/**
 * Odbiera komendy sterujące wysłane przez Home Assistant na topicach
 * {@code smarthome/device/<id>/set} i wywołuje {@link Device#changeState(DeviceState)}
 * dokładnie tak, jak dzisiejszy endpoint {@code POST /api/changeDeviceState}.
 */
@Service
public class MqttCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(MqttCommandHandler.class);
    private final MqttGateway gateway;
    private final SystemDAO systemDAO;
    private final MqttStatePublisher statePublisher;
    // changeState robi synchroniczne I/O na I2C - nie może blokować wątku callbacków Paho;
    // jeden wątek zachowuje kolejność komend
    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mqtt-command");
        t.setDaemon(true);
        return t;
    });

    public MqttCommandHandler(MqttGateway gateway, SystemDAO systemDAO, MqttStatePublisher statePublisher) {
        this.gateway = gateway;
        this.systemDAO = systemDAO;
        this.statePublisher = statePublisher;
    }

    @PreDestroy
    public void stop() {
        commandExecutor.shutdown();
    }

    public void subscribe() {
        gateway.subscribe(MqttTopics.deviceCommandTopicFilter(gateway.getBaseTopic()), this::handleMessage);
    }

    private void handleMessage(String topic, MqttMessage message) {
        Integer deviceId = MqttTopics.parseDeviceIdFromCommandTopic(gateway.getBaseTopic(), topic);
        if (deviceId == null) {
            logger.warn("Nie udało się odczytać id urządzenia z topicu {}", topic);
            return;
        }
        Device device = systemDAO.getDeviceByID(deviceId);
        if (device == null) {
            logger.warn("Otrzymano komendę MQTT dla nieznanego urządzenia id={}", deviceId);
            return;
        }
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        DeviceState state = MqttTopics.commandPayloadToState(device.getTyp(), payload);
        if (state == null) {
            logger.warn("Nieznana komenda MQTT '{}' dla urządzenia id={}", payload, deviceId);
            return;
        }
        commandExecutor.execute(() -> executeCommand(device, state));
    }

    private void executeCommand(Device device, DeviceState state) {
        try {
            logger.debug("Zmieniam stan urządzenia id={} na {} (komenda z MQTT)", device.getId(), state);
            device.changeState(state);
            // potwierdzenie stanu do HA od razu, a nie przy następnym cyklu publishera
            statePublisher.publishDeviceNow(device);
        } catch (Exception e) {
            logger.error("Błąd podczas wykonywania komendy MQTT dla urządzenia id={}: {}", device.getId(), e.getMessage(), e);
        }
    }
}
