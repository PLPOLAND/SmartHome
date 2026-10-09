package newsmarthome.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.Higrometr;
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
        HaDiscoveryPublisher before = publisher(offline, systemDAO(), file);
        before.removeDevice(7, DeviceTypes.LIGHT);
        assertEquals(Collections.singletonList(LIGHT_TOPIC), Files.readAllLines(file, StandardCharsets.UTF_8));

        // "restart": nowa instancja wczytuje zaległe usunięcie i czyści je po połączeniu
        MqttGateway online = gateway(true);
        HaDiscoveryPublisher after = publisher(online, systemDAO(), file);
        after.publishAll();
        verify(online).publish(LIGHT_TOPIC, "", true);
        assertTrue(Files.readAllLines(file, StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    void pendingRemovalIsKeptWhenReplacementConfigFailsToPublish() throws Exception {
        Path file = tempDir.resolve("db/pending.txt");
        HaDiscoveryPublisher publisher = publisher(gateway(false), systemDAO(), file);
        publisher.removeDevice(7, DeviceTypes.LIGHT);

        publisher.publishDevice(light(1));

        assertEquals(Collections.singletonList(LIGHT_TOPIC), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void pendingRemovalFailingAgainOnReconnectStillBlocksReplacement() throws Exception {
        Path pending = tempDir.resolve("pending.txt");
        Files.write(pending, Collections.singletonList(LIGHT_TOPIC), StandardCharsets.UTF_8);
        Files.write(tempDir.resolve("identifiers.txt"),
                java.util.Arrays.asList("# migrated", LIGHT_TOPIC + " smarthome_device_7_room_1"), StandardCharsets.UTF_8);
        MqttGateway gateway = gateway(true);
        when(gateway.publish(LIGHT_TOPIC, "", true)).thenReturn(false, true);
        Light light = light(1);
        SystemDAO systemDAO = systemDAOWith(light);
        when(systemDAO.getDevicesSnapshot()).thenReturn(new ArrayList<>(Collections.singletonList(light)));
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAO, pending);

        publisher.publishAll();

        // nowy config nie może wyprzedzić nieudanego usunięcia - recreate ponawia je i dopiero publikuje
        verify(gateway, never()).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), anyBoolean());
        InOrder order = inOrder(gateway);
        order.verify(gateway, timeout(2000).times(2)).publish(LIGHT_TOPIC, "", true);
        order.verify(gateway, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
    }

    @Test
    void pendingRemovalOfOneHygrometerEntityAlsoHoldsItsSibling() throws Exception {
        String temperatureTopic = "homeassistant/sensor/smarthome_sensor_20/config";
        String humidityTopic = "homeassistant/sensor/smarthome_sensor_20_humidity/config";
        Path pending = tempDir.resolve("pending.txt");
        // przed restartem przeszło tylko usunięcie temperatury
        Files.write(pending, Collections.singletonList(humidityTopic), StandardCharsets.UTF_8);
        Files.write(tempDir.resolve("identifiers.txt"), java.util.Arrays.asList("# migrated",
                temperatureTopic + " smarthome_sensor_20_room_-1", humidityTopic + " smarthome_sensor_20_room_-1"),
                StandardCharsets.UTF_8);
        MqttGateway gateway = gateway(true);
        Higrometr higrometr = new Higrometr();
        higrometr.setId(20);
        SystemDAO systemDAO = systemDAO();
        when(systemDAO.getSensor(20)).thenReturn(higrometr);
        when(systemDAO.getSensorsSnapshot()).thenReturn(new ArrayList<>(Collections.singletonList(higrometr)));
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAO, pending);

        publisher.publishAll();

        verify(gateway, never()).publish(eq(temperatureTopic), contains("temperature"), anyBoolean());
        verify(gateway, timeout(2000)).publish(eq(temperatureTopic), contains("temperature"), eq(true));
        verify(gateway, timeout(2000)).publish(eq(humidityTopic), contains("humidity"), eq(true));
    }

    @Test
    void unknownIdentifierRemovesConfigAndRecreatesItAfterDelay() {
        // config sprzed identyfikatorów z pokojem (albo nowy obiekt) - HA musi utworzyć urządzenie od nowa
        MqttGateway online = gateway(true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(online, systemDAOWith(light), tempDir.resolve("pending.txt"));

        publisher.publishDevice(light);

        InOrder order = inOrder(online);
        order.verify(online).publish(LIGHT_TOPIC, "", true);
        order.verify(online, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
    }

    @Test
    void afterMigrationNewObjectIsPublishedWithoutRemoval() {
        MqttGateway online = gateway(true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(online, systemDAOWith(light), tempDir.resolve("pending.txt"));
        publisher.publishAll(); // pusty system - migracja zakończona, plik identyfikatorów zapisany

        publisher.publishDevice(light);

        verify(online, never()).publish(LIGHT_TOPIC, "", true);
        verify(online).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
        assertTrue(Files.exists(tempDir.resolve("identifiers.txt")));
    }

    @Test
    void identifiersFileWithoutMigrationMarkerKeepsMigrationPending() {
        Path pending = tempDir.resolve("pending.txt");
        // plik zapisany przed pierwszym pełnym publishAll (np. czujnik dodany przy niedostępnym brokerze)
        HaDiscoveryPublisher before = publisher(gateway(false), systemDAO(), pending);
        Button button = new Button(8, 3);
        button.setId(12);
        before.publishSensor(button);
        before.stop();
        assertTrue(Files.exists(tempDir.resolve("identifiers.txt")));

        MqttGateway online = gateway(true);
        Light legacy = light(1);
        HaDiscoveryPublisher after = publisher(online, systemDAOWith(legacy), pending);
        after.publishDevice(legacy);

        // stary config urządzenia nadal musi zostać odtworzony z nowym identyfikatorem
        verify(online).publish(LIGHT_TOPIC, "", true);
    }

    @Test
    void offlineRecreateStopsAndReconnectFinishesIt() {
        Path pending = tempDir.resolve("pending.txt");
        MqttGateway gateway = gateway(false);
        Light light = light(1);
        SystemDAO systemDAO = systemDAOWith(light);
        when(systemDAO.getDevicesSnapshot()).thenReturn(new ArrayList<>(Collections.singletonList(light)));
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAO, pending);

        publisher.publishDevice(light);
        // bez połączenia odtworzenie nie ponawia się w kółko
        verify(gateway, after(300).times(2)).publish(LIGHT_TOPIC, "", true);

        when(gateway.publish(anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(gateway.isConnected()).thenReturn(true);
        publisher.publishAll();

        // publishAll czyści zaległe usunięcie i odtwarza urządzenie dopiero po opóźnieniu
        verify(gateway, times(3)).publish(LIGHT_TOPIC, "", true);
        verify(gateway, never()).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), anyBoolean());
        verify(gateway, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
    }

    @Test
    void reconnectFinishesRecreateStalledAfterPartialRemoval() {
        String temperatureTopic = "homeassistant/sensor/smarthome_sensor_20/config";
        String humidityTopic = "homeassistant/sensor/smarthome_sensor_20_humidity/config";
        MqttGateway gateway = gateway(true);
        // połączenie zrywa się między usunięciem encji temperatury a wilgotności
        when(gateway.publish(humidityTopic, "", true)).thenReturn(false);
        when(gateway.isConnected()).thenReturn(false);
        Higrometr higrometr = new Higrometr();
        higrometr.setId(20);
        SystemDAO systemDAO = systemDAO();
        when(systemDAO.getSensor(20)).thenReturn(higrometr);
        when(systemDAO.getSensorsSnapshot()).thenReturn(new ArrayList<>(Collections.singletonList(higrometr)));
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAO, tempDir.resolve("pending.txt"));

        publisher.publishSensor(higrometr);
        verify(gateway, after(300).times(2)).publish(humidityTopic, "", true);

        when(gateway.publish(humidityTopic, "", true)).thenReturn(true);
        when(gateway.isConnected()).thenReturn(true);
        publisher.publishAll();

        // obie encje wracają, także temperatura, której usunięcie przeszło przed zerwaniem
        verify(gateway, timeout(2000)).publish(eq(temperatureTopic), contains("temperature"), eq(true));
        verify(gateway, timeout(2000)).publish(eq(humidityTopic), contains("humidity"), eq(true));
    }

    @Test
    void unchangedIdentifierIsPublishedDirectly() {
        MqttGateway online = gateway(true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(online, systemDAOWith(light), tempDir.resolve("pending.txt"));
        publisher.publishDevice(light);
        verify(online, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));

        // np. zmiana nazwy - bez usuwania urządzenia z HA
        publisher.publishDevice(light);

        verify(online, times(1)).publish(LIGHT_TOPIC, "", true);
        verify(online, times(2)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
    }

    @Test
    void roomChangeRecreatesDeviceWithNewIdentifierAlsoAfterRestart() {
        Path pending = tempDir.resolve("pending.txt");
        MqttGateway online = gateway(true);
        Light light = light(1);
        HaDiscoveryPublisher before = publisher(online, systemDAOWith(light), pending);
        before.publishDevice(light);
        verify(online, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
        before.stop();

        // pokój zmieniony przed restartem - zapisane identyfikatory pozwalają to wykryć
        light.setRoom(2);
        MqttGateway restarted = gateway(true);
        HaDiscoveryPublisher after = publisher(restarted, systemDAOWith(light), pending);
        after.publishDevice(light);

        InOrder order = inOrder(restarted);
        order.verify(restarted).publish(LIGHT_TOPIC, "", true);
        order.verify(restarted, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_2"), eq(true));
    }

    @Test
    void publishDuringRecreateWindowIsDeferredToScheduledRecreate() throws Exception {
        MqttGateway online = gateway(true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(online, systemDAOWith(light), tempDir.resolve("pending.txt"));
        ReflectionTestUtils.setField(publisher, "recreateDelayMs", 300L);

        publisher.publishDevice(light);
        // np. zmiana nazwy tuż po zmianie pokoju - nie może wyprzedzić usunięcia urządzenia w HA
        publisher.publishDevice(light);
        verify(online, never()).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), anyBoolean());

        verify(online, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true));
        verify(online, after(400).times(1)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true));
    }

    @Test
    void failedRemovalIsRetriedBeforeRecreate() {
        MqttGateway gateway = gateway(true);
        when(gateway.publish(LIGHT_TOPIC, "", true)).thenReturn(false, true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAOWith(light), tempDir.resolve("pending.txt"));

        publisher.publishDevice(light);

        InOrder order = inOrder(gateway);
        order.verify(gateway, timeout(2000).times(2)).publish(LIGHT_TOPIC, "", true);
        order.verify(gateway, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true));
    }

    @Test
    void removalRejectedWhileConnectedGivesUpAndRestoresEntity() {
        MqttGateway gateway = gateway(true);
        // np. ACL brokera odrzuca pusty config - po kilku próbach encja ma wrócić, choćby w starym obszarze
        when(gateway.publish(LIGHT_TOPIC, "", true)).thenReturn(false);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAOWith(light), tempDir.resolve("pending.txt"));

        publisher.publishDevice(light);

        verify(gateway, timeout(2000)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true));
        verify(gateway, times(6)).publish(LIGHT_TOPIC, "", true);
    }

    @Test
    void failedReplacementPublishIsRetried() {
        MqttGateway gateway = gateway(true);
        // usunięcie przeszło, ale publikacja nowego configu nie (np. timeout przy działającym połączeniu)
        when(gateway.publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), eq(true))).thenReturn(false, true);
        Light light = light(1);
        HaDiscoveryPublisher publisher = publisher(gateway, systemDAOWith(light), tempDir.resolve("pending.txt"));

        publisher.publishDevice(light);

        verify(gateway, timeout(2000).times(2)).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7_room_1"), eq(true));
        verify(gateway, times(1)).publish(LIGHT_TOPIC, "", true);
    }

    @Test
    void recreateSkipsDeviceRemovedInTheMeantime() {
        MqttGateway online = gateway(true);
        SystemDAO systemDAO = systemDAO();
        HaDiscoveryPublisher publisher = publisher(online, systemDAO, tempDir.resolve("pending.txt"));

        publisher.publishDevice(light(1));

        // opóźnione zadanie sprawdza, czy urządzenie nadal istnieje (mock zwraca null)
        verify(systemDAO, timeout(2000)).getDeviceByID(7);
        verify(online, never()).publish(eq(LIGHT_TOPIC), contains("smarthome_device_7"), anyBoolean());
    }

    @Test
    void buttonDiscoveryUsesEventComponent() {
        MqttGateway online = gateway(true);
        Button button = new Button(8, 3);
        button.setId(12);
        SystemDAO systemDAO = systemDAO();
        when(systemDAO.getSensor(12)).thenReturn(button);
        HaDiscoveryPublisher publisher = publisher(online, systemDAO, tempDir.resolve("pending.txt"));

        publisher.publishSensor(button);
        verify(online, timeout(2000)).publish(eq(BUTTON_TOPIC), contains("event_types"), eq(true));

        publisher.removeSensor(12, SensorsTypes.BUTTON);
        verify(online, times(2)).publish(BUTTON_TOPIC, "", true);
    }

    private Light light(int roomId) {
        Light light = new Light();
        light.setId(7);
        light.setRoom(roomId);
        return light;
    }

    private MqttGateway gateway(boolean connected) {
        MqttGateway gateway = mock(MqttGateway.class);
        when(gateway.getBaseTopic()).thenReturn("smarthome");
        when(gateway.publish(anyString(), anyString(), anyBoolean())).thenReturn(connected);
        when(gateway.isConnected()).thenReturn(connected);
        return gateway;
    }

    private SystemDAO systemDAO() {
        SystemDAO systemDAO = mock(SystemDAO.class);
        when(systemDAO.getDevicesSnapshot()).thenReturn(new ArrayList<>());
        when(systemDAO.getSensorsSnapshot()).thenReturn(new ArrayList<>());
        return systemDAO;
    }

    private SystemDAO systemDAOWith(Light light) {
        SystemDAO systemDAO = systemDAO();
        when(systemDAO.getDeviceByID(light.getId())).thenReturn(light);
        return systemDAO;
    }

    private HaDiscoveryPublisher publisher(MqttGateway gateway, SystemDAO systemDAO, Path pendingFile) {
        HaDiscoveryPublisher publisher = new HaDiscoveryPublisher(gateway, systemDAO);
        ReflectionTestUtils.setField(publisher, "discoveryPrefix", "homeassistant");
        ReflectionTestUtils.setField(publisher, "pendingRemovalsFile", pendingFile.toString());
        ReflectionTestUtils.setField(publisher, "publishedIdentifiersFile",
                pendingFile.resolveSibling("identifiers.txt").toString());
        ReflectionTestUtils.setField(publisher, "recreateDelayMs", 50L);
        publisher.loadState();
        return publisher;
    }
}
