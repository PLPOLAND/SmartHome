package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.SensorsTypes;

class HaDiscoveryPublisherTest {

    private static final String LIGHT_TOPIC = "homeassistant/light/smarthome_device_7/config";
    private static final String BUTTON_TOPIC = "homeassistant/event/smarthome_sensor_12/config";

    @TempDir
    Path tempDir;

    @Test
    void pendingRemovalSurvivesRestartAndIsClearedAfterReconnect() throws Exception {
        Path file = tempDir.resolve("db/pending.txt");

        MqttGateway offline = gateway(false);
        HaDiscoveryPublisher before = publisher(offline, file);
        before.removeDevice(7, DeviceTypes.LIGHT);
        assertEquals(Collections.singletonList(LIGHT_TOPIC), Files.readAllLines(file, StandardCharsets.UTF_8));

        // "restart": nowa instancja wczytuje zaległe usunięcie i czyści je po połączeniu
        MqttGateway online = gateway(true);
        HaDiscoveryPublisher after = publisher(online, file);
        after.publishAll();
        verify(online).publish(LIGHT_TOPIC, "", true);
        assertTrue(Files.readAllLines(file, StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    void pendingRemovalIsKeptWhenReplacementConfigFailsToPublish() throws Exception {
        Path file = tempDir.resolve("db/pending.txt");
        MqttGateway offline = gateway(false);
        HaDiscoveryPublisher publisher = publisher(offline, file);
        publisher.removeDevice(7, DeviceTypes.LIGHT);

        Light light = new Light();
        light.setId(7);
        publisher.publishDevice(light);

        assertEquals(Collections.singletonList(LIGHT_TOPIC), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void moveDeviceRemovesConfigAndRecreatesItAfterDelay() throws Exception {
        MqttGateway online = gateway(true);
        Light light = new Light();
        light.setId(7);
        SystemDAO systemDAO = systemDAO();
        when(systemDAO.getDeviceByID(7)).thenReturn(light);
        HaDiscoveryPublisher publisher = publisher(online, systemDAO, tempDir.resolve("pending.txt"));

        publisher.moveDevice(light);

        InOrder order = inOrder(online);
        order.verify(online).publish(LIGHT_TOPIC, "", true);
        order.verify(online, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true));
    }

    @Test
    void moveDeviceDoesNotRecreateDeviceRemovedInTheMeantime() throws Exception {
        MqttGateway online = gateway(true);
        Light light = new Light();
        light.setId(7);
        SystemDAO systemDAO = systemDAO();
        HaDiscoveryPublisher publisher = publisher(online, systemDAO, tempDir.resolve("pending.txt"));

        publisher.moveDevice(light);

        // opóźnione zadanie sprawdza, czy urządzenie nadal istnieje (mock zwraca null)
        verify(systemDAO, timeout(2000)).getDeviceByID(7);
        verify(online, never()).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), anyBoolean());
    }

    @Test
    void moveDeviceWhileOfflineKeepsRemovalPendingForReconnect() throws Exception {
        Path file = tempDir.resolve("pending.txt");
        MqttGateway offline = gateway(false);
        Light light = new Light();
        light.setId(7);
        HaDiscoveryPublisher publisher = publisher(offline, systemDAO(), file);

        publisher.moveDevice(light);

        assertEquals(Collections.singletonList(LIGHT_TOPIC), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void buttonDiscoveryUsesEventComponent() throws Exception {
        MqttGateway online = gateway(true);
        HaDiscoveryPublisher publisher = publisher(online, systemDAO(), tempDir.resolve("pending.txt"));
        Button button = new Button(8, 3);
        button.setId(12);

        publisher.publishSensor(button);
        verify(online).publish(eq(BUTTON_TOPIC), contains("event_types"), eq(true));

        publisher.removeSensor(12, SensorsTypes.BUTTON);
        verify(online).publish(BUTTON_TOPIC, "", true);
    }

    private MqttGateway gateway(boolean connected) {
        MqttGateway gateway = mock(MqttGateway.class);
        when(gateway.getBaseTopic()).thenReturn("smarthome");
        when(gateway.publish(anyString(), anyString(), anyBoolean())).thenReturn(connected);
        return gateway;
    }

    private SystemDAO systemDAO() {
        SystemDAO systemDAO = mock(SystemDAO.class);
        when(systemDAO.getDevicesSnapshot()).thenReturn(new ArrayList<>());
        when(systemDAO.getSensorsSnapshot()).thenReturn(new ArrayList<>());
        return systemDAO;
    }

    private HaDiscoveryPublisher publisher(MqttGateway gateway, Path file) {
        return publisher(gateway, systemDAO(), file);
    }

    private HaDiscoveryPublisher publisher(MqttGateway gateway, SystemDAO systemDAO, Path file) {
        HaDiscoveryPublisher publisher = new HaDiscoveryPublisher(gateway, systemDAO);
        ReflectionTestUtils.setField(publisher, "recreateDelayMs", 50L);
        ReflectionTestUtils.setField(publisher, "discoveryPrefix", "homeassistant");
        ReflectionTestUtils.setField(publisher, "pendingRemovalsFile", file.toString());
        publisher.loadPendingRemovals();
        return publisher;
    }
}
