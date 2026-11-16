package newsmarthome.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import newsmarthome.database.SystemDAO;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.mqtt.BridgeConfigPublisher;
import newsmarthome.mqtt.HaDiscoveryPublisher;
import newsmarthome.runners.Runners;

class ConfigurationServiceTest {

    private static final int SLAVE = 18;
    private static final int OTHER_SLAVE = 19;

    private SystemDAO systemDAO;
    private HaDiscoveryPublisher discovery;
    private BridgeConfigPublisher snapshot;
    private Runners runners;
    private ConfigurationService service;
    private Room salon;
    private Room kuchnia;
    private Light light;
    private Button button;
    private final List<Device> devices = new ArrayList<>();
    private final ArrayList<Button> buttons = new ArrayList<>();

    @BeforeEach
    void setUp() {
        systemDAO = mock(SystemDAO.class);
        discovery = mock(HaDiscoveryPublisher.class);
        snapshot = mock(BridgeConfigPublisher.class);
        runners = mock(Runners.class);
        service = new ConfigurationService(systemDAO, discovery, snapshot, runners, mock(MasterToSlaveConverter.class));

        salon = new Room(1, "Salon");
        kuchnia = new Room(2, "Kuchnia");
        light = new Light(DeviceState.OFF, 5, SLAVE);
        light.setId(7);
        light.setName("Lampa");
        salon.addDevice(light);
        devices.add(light);
        button = new Button(SLAVE, 9);
        button.setId(12);
        button.setNazwa("Włącznik");
        salon.addSensor(button);
        buttons.add(button);

        when(systemDAO.getRoom(1)).thenReturn(salon);
        when(systemDAO.getRoom(2)).thenReturn(kuchnia);
        when(systemDAO.getRoom("Salon")).thenReturn(salon);
        when(systemDAO.getDeviceByID(7)).thenReturn(light);
        when(systemDAO.getSensor(12)).thenReturn(button);
        when(systemDAO.getDevicesSnapshot()).thenReturn(devices);
        when(systemDAO.getAllButtons()).thenReturn(buttons);
        when(systemDAO.removeDevice(7)).thenReturn(true);
        when(runners.reconfigureSlave(anyInt())).thenReturn(true);
    }

    @Test
    void addDeviceRejectsPinTakenByDeviceOrButtonOnSameSlave() {
        ConfigurationException byDevice = assertThrows(ConfigurationException.class,
                () -> service.addDevice("Nowa", DeviceTypes.LIGHT, 1, SLAVE, 5, null));
        assertTrue(byDevice.getMessage().contains("Lampa"));
        assertThrows(ConfigurationException.class, () -> service.addDevice("Nowa", DeviceTypes.LIGHT, 1, SLAVE, 9, null));
        assertThrows(ConfigurationException.class, () -> service.addDevice("Roleta", DeviceTypes.BLIND, 1, SLAVE, 3, 3));
        assertThrows(ConfigurationException.class, () -> service.addDevice("Roleta", DeviceTypes.BLIND, 1, SLAVE, 3, null));
        verify(systemDAO, never()).addDevice(any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    void addDeviceOnOtherSlaveMayReuseThePin() {
        Light created = new Light(DeviceState.OFF, 5, OTHER_SLAVE);
        when(systemDAO.addDevice(salon, "Nowa", DeviceTypes.LIGHT, OTHER_SLAVE, 5, null)).thenReturn(created);

        assertSame(created, service.addDevice(" Nowa ", DeviceTypes.LIGHT, 1, OTHER_SLAVE, 5, null));

        verify(discovery).publishDevice(created);
        verify(snapshot).requestPublish();
        // dodanie wysyła na slave'a tylko nowe urządzenie - bez resetu pozostałych
        verify(runners, never()).reconfigureSlave(anyInt());
    }

    @Test
    void renamingDeviceDoesNotTouchTheSlave() {
        service.updateDevice(7, "Żyrandol", null, null, null, null);

        assertEquals("Żyrandol", light.getName());
        verify(systemDAO).save(salon);
        verify(discovery).publishDevice(light);
        verify(runners, never()).reconfigureSlave(anyInt());
    }

    @Test
    void changingPinAndSlaveReconfiguresBothSlaves() {
        service.updateDevice(7, null, 2, OTHER_SLAVE, 6, null);

        assertEquals(6, light.getPin());
        assertEquals(OTHER_SLAVE, light.getSlaveID());
        assertEquals(2, light.getRoom());
        assertTrue(kuchnia.getDevices().contains(light));
        assertTrue(salon.getDevices().isEmpty());
        verify(runners).reconfigureSlave(SLAVE);
        verify(runners).reconfigureSlave(OTHER_SLAVE);
    }

    @Test
    void movedDeviceForgetsItsSlotOnTheOldSlave() {
        // slot 3 na nowej płytce należy do innego urządzenia - nie wolno nim sterować
        light.setOnSlaveID(3);
        light.setConfigured();

        service.updateDevice(7, null, null, OTHER_SLAVE, null, null);

        assertEquals(-1, light.getOnSlaveID());
        assertFalse(light.isConfigured());
    }

    @Test
    void movedButtonForgetsItsSlotOnTheOldSlave() {
        button.setOnSlaveID(2);

        service.updateSensor(12, null, null, OTHER_SLAVE, null, null);

        assertEquals(OTHER_SLAVE, button.getSlaveAdress());
        assertEquals(-1, button.getOnSlaveID());
        verify(runners).reconfigureSlave(SLAVE);
        verify(runners).reconfigureSlave(OTHER_SLAVE);
    }

    @Test
    void renamingKeepsTheSlot() {
        light.setOnSlaveID(3);
        light.setConfigured();

        service.updateDevice(7, "Żyrandol", 2, null, null, null);

        assertEquals(3, light.getOnSlaveID());
        assertTrue(light.isConfigured());
    }

    @Test
    void invalidUpdateLeavesDeviceUntouched() {
        assertThrows(ConfigurationException.class, () -> service.updateDevice(7, "Nowa nazwa", 99, null, 6, null));
        assertThrows(ConfigurationException.class, () -> service.updateDevice(7, "Nowa nazwa", null, null, 9, null));
        assertThrows(ConfigurationException.class, () -> service.updateDevice(7, "Nowa nazwa", null, null, null, 4));

        assertEquals("Lampa", light.getName());
        assertEquals(5, light.getPin());
        verify(discovery, never()).publishDevice(any());
    }

    @Test
    void removingDeviceDropsClickFunctionsThatControlledIt() {
        light.setOnSlaveID(3);
        light.setConfigured();
        button.addFunkcjaKilkniecia(new ButtonLocalFunction(button, light, ButtonLocalFunction.State.NONE, 1));

        service.removeDevice(7);

        assertTrue(button.getFunkcjeKlikniec().isEmpty());
        verify(systemDAO).save(salon);
        // automatyzacje serwera mogą dalej trzymać ten obiekt - nie może już wysyłać komend
        assertEquals(-1, light.getOnSlaveID());
        assertFalse(light.isConfigured());
        verify(discovery).removeDevice(7, DeviceTypes.LIGHT);
        verify(runners).reconfigureSlave(SLAVE);
    }

    @Test
    void clickFunctionMustControlDeviceOnButtonsSlave() {
        Light remote = new Light(DeviceState.OFF, 2, OTHER_SLAVE);
        remote.setId(8);
        when(systemDAO.getDeviceByID(8)).thenReturn(remote);
        List<ClickFunctionSpec> onOtherSlave = Collections
                .singletonList(new ClickFunctionSpec(1, 8, ButtonLocalFunction.State.NONE));
        List<ClickFunctionSpec> duplicated = Arrays.asList(new ClickFunctionSpec(1, 7, ButtonLocalFunction.State.NONE),
                new ClickFunctionSpec(1, 7, ButtonLocalFunction.State.NONE));

        assertThrows(ConfigurationException.class, () -> service.updateSensor(12, null, null, null, null, onOtherSlave));
        assertThrows(ConfigurationException.class, () -> service.updateSensor(12, null, null, null, null, duplicated));
        assertTrue(button.getFunkcjeKlikniec().isEmpty());
    }

    @Test
    void changedClickFunctionsAreSentToSlaveButUnchangedOnesAreNot() {
        List<ClickFunctionSpec> functions = Collections
                .singletonList(new ClickFunctionSpec(2, 7, ButtonLocalFunction.State.NONE));

        service.updateSensor(12, null, null, null, null, functions);

        assertEquals(1, button.getFunkcjeKlikniec().size());
        assertSame(button, button.getFunkcjeKlikniec().get(0).getButton());
        verify(runners).reconfigureSlave(SLAVE);

        service.updateSensor(12, "Nowa nazwa", null, null, null, functions);
        verify(runners).reconfigureSlave(SLAVE); // nadal tylko raz
    }

    @Test
    void hygrometerReservesItsFixedPin() {
        Higrometr higrometr = new Higrometr();
        higrometr.setSlaveAdress(OTHER_SLAVE);
        when(systemDAO.getAllHigrometers()).thenReturn(Collections.singletonList(higrometr));

        // pin DHT jest zajęty na płytce z higrometrem, ale wolny na pozostałych
        assertThrows(ConfigurationException.class,
                () -> service.addDevice("Nowa", DeviceTypes.LIGHT, 1, OTHER_SLAVE, ConfigurationService.HIGROMETR_PIN, null));
        assertThrows(ConfigurationException.class, () -> service.addSensor(SensorsTypes.BUTTON, "P", 1, OTHER_SLAVE,
                ConfigurationService.HIGROMETR_PIN, null));
        assertThrows(ConfigurationException.class,
                () -> service.addSensor(SensorsTypes.THERMOMETR_HYGROMETR, "Drugi", 1, OTHER_SLAVE, null, null));

        // i odwrotnie: higrometr nie może trafić na płytkę, gdzie jego pin ma urządzenie
        service.updateDevice(7, null, null, null, ConfigurationService.HIGROMETR_PIN, null);
        assertThrows(ConfigurationException.class,
                () -> service.addSensor(SensorsTypes.THERMOMETR_HYGROMETR, "H", 1, SLAVE, null, null));
        verify(systemDAO, never()).addHigrometr(any(), any(), anyInt());
    }

    @Test
    void addSensorRejectsManualThermometer() {
        assertThrows(ConfigurationException.class,
                () -> service.addSensor(SensorsTypes.THERMOMETR, "T", 1, SLAVE, null, null));
    }

    @Test
    void roomRules() {
        Room brak = new Room(0, ConfigurationService.DEFAULT_ROOM_NAME);
        when(systemDAO.getRoom(0)).thenReturn(brak);

        assertThrows(ConfigurationException.class, () -> service.addRoom("Salon"));
        assertThrows(ConfigurationException.class, () -> service.addRoom("  "));
        assertThrows(ConfigurationException.class, () -> service.removeRoom(1)); // niepusty
        assertThrows(ConfigurationException.class, () -> service.removeRoom(0));
        assertThrows(ConfigurationException.class, () -> service.renameRoom(0, "Inny"));
        assertThrows(ConfigurationException.class, () -> service.renameRoom(2, "Salon"));

        when(systemDAO.removeRoom(kuchnia)).thenReturn(true);
        service.removeRoom(2);
        verify(systemDAO).removeRoom(kuchnia);

        service.renameRoom(1, "Pokój dzienny");
        verify(systemDAO).renameRoom(salon, "Pokój dzienny");
        verify(discovery).publishDevice(light);
        verify(discovery).publishSensor(button);
    }
}
