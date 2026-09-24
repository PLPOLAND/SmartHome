package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.DeviceTypes;

class HaDiscoveryPublisherTest {

    private static final String LIGHT_TOPIC = "homeassistant/light/smarthome_device_7/config";

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

    private MqttGateway gateway(boolean connected) {
        MqttGateway gateway = mock(MqttGateway.class);
        when(gateway.getBaseTopic()).thenReturn("smarthome");
        when(gateway.publish(anyString(), anyString(), anyBoolean())).thenReturn(connected);
        return gateway;
    }

    private HaDiscoveryPublisher publisher(MqttGateway gateway, Path file) {
        SystemDAO systemDAO = mock(SystemDAO.class);
        when(systemDAO.getDevicesSnapshot()).thenReturn(new ArrayList<>());
        when(systemDAO.getSensorsSnapshot()).thenReturn(new ArrayList<>());
        HaDiscoveryPublisher publisher = new HaDiscoveryPublisher(gateway, systemDAO);
        ReflectionTestUtils.setField(publisher, "discoveryPrefix", "homeassistant");
        ReflectionTestUtils.setField(publisher, "pendingRemovalsFile", file.toString());
        publisher.loadPendingRemovals();
        return publisher;
    }
}
