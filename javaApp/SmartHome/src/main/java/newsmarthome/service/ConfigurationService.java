package newsmarthome.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import newsmarthome.database.SystemDAO;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.mqtt.BridgeConfigPublisher;
import newsmarthome.mqtt.HaDiscoveryPublisher;
import newsmarthome.runners.Runners;

/**
 * Jedyne miejsce z logiką zmian konfiguracji (pokoje, urządzenia, czujniki) - wspólne dla REST
 * (aplikacja mobilna) i MQTT (integracja Home Assistant). Każda zmiana jest walidowana w całości
 * przed pierwszą modyfikacją, zapisywana do plików pokoi, publikowana do HA (discovery + snapshot)
 * i - jeśli dotyczy sprzętu - wysyłana na slave'a.
 *
 * <p>Firmware nie potrafi usunąć ani zmienić pojedynczego urządzenia, więc zmiana pinu/slave'a,
 * usunięcie oraz zmiana funkcji kliknięć kończą się ponownym wysłaniem całej konfiguracji slave'a
 * ({@link Runners#reconfigureSlave(int)}), co na chwilę resetuje jego wyjścia.
 */
@Service
public class ConfigurationService {

    /** Pokój techniczny: trafiają do niego automatycznie wykryte termometry, musi istnieć pod tą nazwą. */
    public static final String DEFAULT_ROOM_NAME = "Brak";

    private final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private final SystemDAO systemDAO;
    private final HaDiscoveryPublisher discoveryPublisher;
    private final BridgeConfigPublisher bridgeConfigPublisher;
    private final Runners runners;
    private final MasterToSlaveConverter slaveSender;

    public ConfigurationService(SystemDAO systemDAO, HaDiscoveryPublisher discoveryPublisher,
            BridgeConfigPublisher bridgeConfigPublisher, Runners runners, MasterToSlaveConverter slaveSender) {
        this.systemDAO = systemDAO;
        this.discoveryPublisher = discoveryPublisher;
        this.bridgeConfigPublisher = bridgeConfigPublisher;
        this.runners = runners;
        this.slaveSender = slaveSender;
    }

    // ---------------------------------------------------------------- pokoje

    public synchronized Room addRoom(String name) {
        String roomName = validRoomName(name);
        Room room = new Room(systemDAO.nextRoomId(), roomName);
        systemDAO.addRoom(room);
        bridgeConfigPublisher.requestPublish();
        return room;
    }

    public synchronized Room renameRoom(int roomId, String name) {
        Room room = requireRoom(roomId);
        if (DEFAULT_ROOM_NAME.equals(room.getName())) {
            throw new ConfigurationException("Nie można zmienić nazwy pokoju '" + DEFAULT_ROOM_NAME + "'");
        }
        if (name != null && name.trim().equals(room.getName())) {
            return room;
        }
        systemDAO.renameRoom(room, validRoomName(name));
        // nazwa pokoju jest w configach discovery (suggested_area)
        new ArrayList<>(room.getDevices()).forEach(discoveryPublisher::publishDevice);
        new ArrayList<>(room.getSensors()).forEach(discoveryPublisher::publishSensor);
        bridgeConfigPublisher.requestPublish();
        return room;
    }

    public synchronized void removeRoom(int roomId) {
        Room room = requireRoom(roomId);
        if (DEFAULT_ROOM_NAME.equals(room.getName())) {
            throw new ConfigurationException("Nie można usunąć pokoju '" + DEFAULT_ROOM_NAME + "'");
        }
        if (!room.getDevices().isEmpty() || !room.getSensors().isEmpty()) {
            throw new ConfigurationException("Pokój nie jest pusty - najpierw przenieś lub usuń jego urządzenia i czujniki");
        }
        if (!systemDAO.removeRoom(room)) {
            throw new ConfigurationException("Nie udało się usunąć pokoju");
        }
        bridgeConfigPublisher.requestPublish();
    }

    // ------------------------------------------------------------- urządzenia

    public synchronized Device addDevice(String name, DeviceTypes type, int roomId, int slaveId, Integer pin,
            Integer pin2) {
        Room room = requireRoom(roomId);
        if (type == null || type == DeviceTypes.NONE) {
            throw new ConfigurationException("Nieznany typ urządzenia");
        }
        String deviceName = validName(name);
        if (pin == null) {
            throw new ConfigurationException("Nie podano pinu");
        }
        if (type == DeviceTypes.BLIND && pin2 == null) {
            throw new ConfigurationException("Roleta wymaga dwóch pinów (pin i pin2)");
        }
        List<Integer> pins = new ArrayList<>();
        pins.add(pin);
        if (type == DeviceTypes.BLIND) {
            pins.add(pin2);
        }
        validatePins(slaveId, pins, null, null);
        Device device = systemDAO.addDevice(room, deviceName, type, slaveId, pin, pin2);
        if (device == null) {
            throw new ConfigurationException("Nie udało się dodać urządzenia");
        }
        discoveryPublisher.publishDevice(device);
        bridgeConfigPublisher.requestPublish();
        return device;
    }

    /** Zmienia podane (nie-null) pola urządzenia. */
    public synchronized Device updateDevice(int deviceId, String name, Integer roomId, Integer slaveId, Integer pin,
            Integer pin2) {
        Device device = requireDevice(deviceId);
        Room newRoom = roomId != null ? requireRoom(roomId) : null;
        String newName = name != null ? validName(name) : null;
        if (pin2 != null && !(device instanceof Blind)) {
            throw new ConfigurationException("pin2 można ustawić tylko dla rolety");
        }
        int oldSlave = device.getSlaveID();
        int newSlave = slaveId != null ? slaveId : oldSlave;
        List<Integer> oldPins = pinsOf(device);
        List<Integer> newPins = new ArrayList<>(oldPins);
        if (pin != null) {
            newPins.set(0, pin);
        }
        if (pin2 != null) {
            newPins.set(1, pin2);
        }
        boolean hardwareChanged = newSlave != oldSlave || !newPins.equals(oldPins);
        if (hardwareChanged) {
            validatePins(newSlave, newPins, device, null);
            if (newSlave != oldSlave && isUsedByClickFunction(device)) {
                throw new ConfigurationException(
                        "Urządzenie jest sterowane lokalną funkcją przycisku - najpierw usuń tę funkcję");
            }
        }

        if (newName != null) {
            device.setName(newName);
        }
        if (pin != null) {
            setPin(device, pin);
        }
        if (pin2 != null) {
            ((Blind) device).setPinDown(pin2);
        }
        if (newSlave != oldSlave) {
            device.setSlaveID(newSlave);
        }
        Room oldRoom = systemDAO.getRoom(device.getRoom());
        if (newRoom != null && newRoom != oldRoom) {
            if (oldRoom != null) {
                oldRoom.delDevice(device);
            }
            newRoom.addDevice(device);
        } else {
            systemDAO.save(oldRoom);
        }
        // nowy pokój zmienia identyfikator urządzenia HA - publisher odtworzy je w nowym obszarze
        discoveryPublisher.publishDevice(device);
        if (hardwareChanged) {
            reconfigureSlaves(oldSlave, newSlave);
        }
        bridgeConfigPublisher.requestPublish();
        return device;
    }

    public synchronized void removeDevice(int deviceId) {
        Device device = requireDevice(deviceId);
        // funkcje kliknięć wskazujące usuwane urządzenie sterowałyby po reinicie innym urządzeniem slave'a
        Set<Room> roomsToSave = new HashSet<>();
        for (Button button : systemDAO.getAllButtons()) {
            if (button.getFunkcjeKlikniec().removeIf(function -> isDevice(function, device))) {
                roomsToSave.add(systemDAO.getRoom(button.getRoom()));
            }
        }
        if (!systemDAO.removeDevice(deviceId)) {
            throw new ConfigurationException("Nie udało się usunąć urządzenia");
        }
        roomsToSave.forEach(systemDAO::save);
        discoveryPublisher.removeDevice(deviceId, device.getTyp());
        reconfigureSlaves(device.getSlaveID());
        bridgeConfigPublisher.requestPublish();
    }

    // --------------------------------------------------------------- czujniki

    public synchronized Sensor addSensor(SensorsTypes type, String name, int roomId, int slaveId, Integer pin,
            List<ClickFunctionSpec> clickFunctions) {
        if (type == SensorsTypes.THERMOMETR) {
            throw new ConfigurationException(
                    "Nie można dodać termometru ręcznie! Termometry dodawane są automatycznie po wykryciu na slave-ie.");
        }
        if (type != SensorsTypes.BUTTON && type != SensorsTypes.THERMOMETR_HYGROMETR) {
            throw new ConfigurationException("Nieobsługiwany typ czujnika");
        }
        Room room = requireRoom(roomId);
        String sensorName = validName(name);
        Sensor sensor;
        if (type == SensorsTypes.BUTTON) {
            if (pin == null) {
                throw new ConfigurationException("Nie podano pinu przycisku");
            }
            List<Integer> pins = new ArrayList<>();
            pins.add(pin);
            validatePins(slaveId, pins, null, null);
            List<ButtonLocalFunction> functions = toFunctions(clickFunctions, slaveId);
            sensor = systemDAO.addButton(room, sensorName, slaveId, pin, functions);
        } else {
            sensor = systemDAO.addHigrometr(room, sensorName, slaveId);
        }
        discoveryPublisher.publishSensor(sensor);
        bridgeConfigPublisher.requestPublish();
        return sensor;
    }

    /** Zmienia podane (nie-null) pola czujnika; slave, pin i funkcje kliknięć tylko dla przycisku. */
    public synchronized Sensor updateSensor(int sensorId, String name, Integer roomId, Integer slaveId, Integer pin,
            List<ClickFunctionSpec> clickFunctions) {
        Sensor sensor = requireSensor(sensorId);
        Room newRoom = roomId != null ? requireRoom(roomId) : null;
        String newName = name != null ? validName(name) : null;
        boolean isButton = sensor instanceof Button;
        if (slaveId != null && !isButton) {
            throw new ConfigurationException("Nie można zmienić slaveID czujnika innego typu niż przycisk");
        }
        if (pin != null && !isButton) {
            throw new ConfigurationException("Nie można zmienić pinu czujnika innego typu niż przycisk");
        }
        if (clickFunctions != null && !isButton) {
            throw new ConfigurationException("Nie można zmienić funkcji kliknięć czujnika innego typu niż przycisk");
        }
        int oldSlave = sensor.getSlaveAdress();
        int newSlave = slaveId != null ? slaveId : oldSlave;
        boolean hardwareChanged = false;
        List<ButtonLocalFunction> functions = null;
        if (isButton) {
            Button button = (Button) sensor;
            int newPin = pin != null ? pin : button.getPin();
            boolean placeChanged = newSlave != oldSlave || newPin != button.getPin();
            if (placeChanged) {
                List<Integer> pins = new ArrayList<>();
                pins.add(newPin);
                validatePins(newSlave, pins, null, button);
            }
            if (clickFunctions != null) {
                functions = toFunctions(clickFunctions, newSlave);
            } else if (newSlave != oldSlave && !button.getFunkcjeKlikniec().isEmpty()) {
                throw new ConfigurationException(
                        "Przycisk ma lokalne funkcje kliknięć urządzeń ze starego slave'a - podaj nowe funkcje albo je usuń");
            }
            hardwareChanged = placeChanged || (functions != null && !sameFunctions(button.getFunkcjeKlikniec(), functions));
        }

        if (newName != null) {
            sensor.setNazwa(newName);
        }
        if (isButton) {
            Button button = (Button) sensor;
            if (newSlave != oldSlave) {
                button.setSlaveAdress(newSlave);
            }
            if (pin != null) {
                button.setPin(pin);
            }
            if (functions != null) {
                button.clearFunkcjeKlikniec();
                button.addFunkcjeKlikniec(functions);
            }
        }
        Room oldRoom = systemDAO.getRoom(sensor.getRoom());
        if (newRoom != null && newRoom != oldRoom) {
            if (oldRoom != null) {
                oldRoom.delSensor(sensor);
            }
            newRoom.addSensor(sensor);
        } else {
            systemDAO.save(oldRoom);
        }
        discoveryPublisher.publishSensor(sensor);
        if (hardwareChanged) {
            reconfigureSlaves(oldSlave, newSlave);
        }
        bridgeConfigPublisher.requestPublish();
        return sensor;
    }

    public synchronized void removeSensor(int sensorId) {
        Sensor sensor = requireSensor(sensorId);
        if (!systemDAO.removeSensor(sensorId)) {
            throw new ConfigurationException("Nie udało się usunąć czujnika");
        }
        discoveryPublisher.removeSensor(sensorId, sensor.getTyp());
        // termometr 1-Wire slave wykrywa sam; przycisk/higrometr zostałby na slave do reinitu
        if (sensor.getTyp() == SensorsTypes.BUTTON || sensor.getTyp() == SensorsTypes.THERMOMETR_HYGROMETR) {
            reconfigureSlaves(sensor.getSlaveAdress());
        }
        bridgeConfigPublisher.requestPublish();
    }

    // ----------------------------------------------------------------- serwis

    public void restartSlaves() {
        slaveSender.restartAllSlaves();
        bridgeConfigPublisher.requestPublish();
    }

    public void reinitSlave(int slaveId) {
        if (!runners.reconfigureSlave(slaveId)) {
            throw new ConfigurationException("Slave " + slaveId + " nie jest podłączony");
        }
        bridgeConfigPublisher.requestPublish();
    }

    // ------------------------------------------------------------- pomocnicze

    private void reconfigureSlaves(int... slaves) {
        for (int slave : new TreeSet<>(toList(slaves))) {
            try {
                if (!runners.reconfigureSlave(slave)) {
                    logger.info("Slave {} nie jest podłączony - konfiguracja zostanie wysłana po jego wykryciu", slave);
                }
            } catch (RuntimeException e) {
                // zmiana jest już zapisana; slave dostanie ją przy następnej konfiguracji
                logger.error("Błąd podczas ponownej konfiguracji slave'a {}: {}", slave, e.getMessage(), e);
            }
        }
    }

    private static List<Integer> toList(int[] values) {
        List<Integer> list = new ArrayList<>();
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    private Room requireRoom(int roomId) {
        Room room = systemDAO.getRoom(roomId);
        if (room == null) {
            throw new ConfigurationException("Nie znaleziono pokoju o podanym ID");
        }
        return room;
    }

    private Device requireDevice(int deviceId) {
        Device device = systemDAO.getDeviceByID(deviceId);
        if (device == null) {
            throw new ConfigurationException("Nie znaleziono urządzenia o podanym ID");
        }
        return device;
    }

    private Sensor requireSensor(int sensorId) {
        Sensor sensor = systemDAO.getSensor(sensorId);
        if (sensor == null) {
            throw new ConfigurationException("Nie znaleziono czujnika o podanym ID");
        }
        return sensor;
    }

    private static String validName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ConfigurationException("Nazwa nie może być pusta");
        }
        return name.trim();
    }

    private String validRoomName(String name) {
        String roomName = validName(name);
        if (systemDAO.getRoom(roomName) != null) {
            throw new ConfigurationException("Pokój o nazwie '" + roomName + "' już istnieje");
        }
        return roomName;
    }

    /** Piny urządzenia: [pin] albo [pinUp, pinDown] dla rolety. */
    public static List<Integer> pinsOf(Device device) {
        List<Integer> pins = new ArrayList<>();
        if (device instanceof Blind) {
            pins.add(((Blind) device).getPinUp());
            pins.add(((Blind) device).getPinDown());
        } else if (device instanceof Light) {
            pins.add(((Light) device).getPin());
        } else if (device instanceof Fan) {
            pins.add(((Fan) device).getPin());
        } else if (device instanceof Outlet) {
            pins.add(((Outlet) device).getPin());
        }
        return pins;
    }

    private static void setPin(Device device, int pin) {
        if (device instanceof Blind) {
            ((Blind) device).setPinUp(pin);
        } else if (device instanceof Light) {
            ((Light) device).setPin(pin);
        } else if (device instanceof Fan) {
            ((Fan) device).setPin(pin);
        } else if (device instanceof Outlet) {
            ((Outlet) device).setPin(pin);
        } else {
            throw new ConfigurationException("Nie udało się zmienić pinu urządzenia: nieznany typ urządzenia");
        }
    }

    /** Odrzuca piny ujemne, powtórzone albo zajęte przez inne urządzenie/przycisk tego slave'a. */
    private void validatePins(int slaveId, List<Integer> pins, Device ignoredDevice, Button ignoredButton) {
        if (new HashSet<>(pins).size() != pins.size()) {
            throw new ConfigurationException("Piny urządzenia muszą być różne");
        }
        for (int pin : pins) {
            if (pin < 0) {
                throw new ConfigurationException("Pin nie może być ujemny");
            }
            for (Device other : systemDAO.getDevicesSnapshot()) {
                if (other != ignoredDevice && other.getSlaveID() == slaveId && pinsOf(other).contains(pin)) {
                    throw new ConfigurationException("Pin " + pin + " na slave " + slaveId
                            + " jest już zajęty przez urządzenie '" + other.getName() + "'");
                }
            }
            for (Button other : systemDAO.getAllButtons()) {
                if (other != ignoredButton && other.getSlaveAdress() == slaveId && other.getPin() == pin) {
                    throw new ConfigurationException("Pin " + pin + " na slave " + slaveId
                            + " jest już zajęty przez przycisk '" + other.getNazwa() + "'");
                }
            }
        }
    }

    /** Po id - funkcje wczytane z pliku mogą trzymać własną kopię obiektu urządzenia. */
    private static boolean isDevice(ButtonLocalFunction function, Device device) {
        return function.getDevice() != null && function.getDevice().getId() == device.getId();
    }

    private boolean isUsedByClickFunction(Device device) {
        for (Button button : systemDAO.getAllButtons()) {
            for (ButtonLocalFunction function : button.getFunkcjeKlikniec()) {
                if (isDevice(function, device)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Funkcję wykonuje slave przycisku, więc może sterować tylko urządzeniem na tym samym slave. */
    private List<ButtonLocalFunction> toFunctions(List<ClickFunctionSpec> specs, int buttonSlave) {
        List<ButtonLocalFunction> functions = new ArrayList<>();
        if (specs == null) {
            return functions;
        }
        Set<Integer> usedClicks = new HashSet<>();
        for (ClickFunctionSpec spec : specs) {
            // kliknięcia liczone są od 1, a clicks trafia do komendy slave-a jako bajt
            if (spec.getClicks() < 1 || spec.getClicks() > 255) {
                throw new ConfigurationException("Niepoprawna liczba kliknięć: " + spec.getClicks());
            }
            if (!usedClicks.add(spec.getClicks())) {
                throw new ConfigurationException("Dwie funkcje dla tej samej liczby kliknięć: " + spec.getClicks());
            }
            Device device = systemDAO.getDeviceByID(spec.getDeviceId());
            if (device == null) {
                throw new ConfigurationException("Funkcja kliknięcia wskazuje nieistniejące urządzenie");
            }
            if (device.getSlaveID() != buttonSlave) {
                throw new ConfigurationException("Urządzenie '" + device.getName()
                        + "' jest na innym slave niż przycisk - lokalna funkcja nie może nim sterować");
            }
            ButtonLocalFunction function = new ButtonLocalFunction();
            function.setClicks(spec.getClicks());
            function.setState(spec.getState());
            function.setDevice(device);
            functions.add(function);
        }
        return functions;
    }

    private static boolean sameFunctions(List<ButtonLocalFunction> current, List<ButtonLocalFunction> updated) {
        if (current.size() != updated.size()) {
            return false;
        }
        for (ButtonLocalFunction a : updated) {
            boolean found = false;
            for (ButtonLocalFunction b : current) {
                if (a.getClicks() == b.getClicks() && a.getDevice() != null && b.getDevice() != null && a.getDevice().getId() == b.getDevice().getId() && a.getState() == b.getState()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
