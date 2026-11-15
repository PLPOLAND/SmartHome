package newsmarthome.mqtt;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Po pełnym starcie aplikacji (kiedy wszystkie pokoje/urządzenia są już wczytane przez
 * {@code SystemDAO}) łączy się z brokerem MQTT, subskrybuje komendy sterujące i publikuje
 * pełną konfigurację Home Assistant MQTT Discovery.
 */
@Component
public class MqttStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final MqttGateway gateway;
    private final HaDiscoveryPublisher discoveryPublisher;
    private final MqttCommandHandler commandHandler;
    private final MqttConfigHandler configHandler;
    private final BridgeConfigPublisher bridgeConfigPublisher;

    public MqttStartupListener(MqttGateway gateway, HaDiscoveryPublisher discoveryPublisher,
            MqttCommandHandler commandHandler, MqttConfigHandler configHandler,
            BridgeConfigPublisher bridgeConfigPublisher) {
        this.gateway = gateway;
        this.discoveryPublisher = discoveryPublisher;
        this.commandHandler = commandHandler;
        this.configHandler = configHandler;
        this.bridgeConfigPublisher = bridgeConfigPublisher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        gateway.addConnectListener(discoveryPublisher::publishAll);
        gateway.addConnectListener(bridgeConfigPublisher::publishAfterConnect);
        gateway.connect();
        commandHandler.subscribe();
        configHandler.subscribe();
    }
}
