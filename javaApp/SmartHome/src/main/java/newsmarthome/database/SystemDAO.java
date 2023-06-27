package newsmarthome.database;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import newsmarthome.model.Room;
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonClickType;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository("systemDAO")
@DependsOn("hardwareFactory")
public class SystemDAO {

    private static final String ROOMS_FILES_LOCALISATION = "smarthome/database/rooms/";
    TreeMap<String, Room> pokoje; // Pokoje w systemie
    Logger logger;

    ArrayList<Device> devices;
    ArrayList<Sensor> sensors;

    @Autowired
    HardwareFactory hardwareFactory;

    // @Autowired
    public SystemDAO( HardwareFactory hardwareFactory) {
        this.hardwareFactory = hardwareFactory;
        logger = LoggerFactory.getLogger(SystemDAO.class);
        logger.info("Init System DAO");
        pokoje = new TreeMap<>();
        devices = new ArrayList<>();
        sensors = new ArrayList<>();
        this.readDatabase();
        if (pokoje.isEmpty()) {
            addRoom(new Room(0, "Brak"));
        }
    }

    /**
     * Dodaje pokój do systemu
     * 
     * @param pokoj - pokój do dodania
     */
    public void addRoom(Room pokoj) {
        this.pokoje.put(pokoj.getName(), pokoj);
        save(pokoj);
    }

    /**
     * Zwraca pokoj o podanej nazwie
     * 
     * @param name - nazwa pokoju
     * @return znaleziony pokoj / null jeśli takiego brak
     */
    public Room getRoom(String name) {
        return this.pokoje.get(name);
    }

    /**
     * Zwraca pokoj o podanym ID
     * 
     * @param id - id pokoju
     * @return znaleziony pokoj / null jeśli takiego brak
     */
    public Room getRoom(int id) {
        for (Room room : this.pokoje.values()) {
            if (room.getID() == id) {
                return room;
            }
        }
        return null;
    }

    /**
     * Usuwa podany pokój z systemu
     * 
     * @param r pokój do usunięcia
     * @return true jeśli taki pokój istniał
     */
    public boolean removeRoom(Room r) {
        if (r == null) {
            return false;
        }
        this.devices.removeAll(r.getDevices());
        this.sensors.removeAll(r.getSensors());
        r.safeDelete();

        if (pokoje.remove(r.getName()) != null) {
            delete(r);
            return true;
        } else
            return false;
    }

    /**
     * Usuwa podany pokój z systemu
     * 
     * @param r nazwa pokoju do usunieca
     * @return true jeśli taki pokój istniał
     */
    public boolean removeRoom(String r) {
        Room id = pokoje.get(r);// znajdź pokój
        if (id != null) {
            return removeRoom(id);// usuń go
        }
        return false;
    }

    /**
     * Zwraca pokoje w systemie w postaci ArrayList
     * 
     * @return ArrayList<Room>
     */
    public ArrayList<Room> getRoomsArrayList() {
        ArrayList<Room> tmp = new ArrayList<>(pokoje.values());
        tmp.sort((a, b) -> (a.getID() < b.getID()) ? -1 : 1);
        return tmp;
    }

    /**
     * Zwraca nazwy pokoi w systemie w postaci ArrayList
     * 
     * @return ArrayList<Room>
     */
    public ArrayList<String> getRoomsNames() {
        ArrayList<Room> tmp = getRoomsArrayList();
        ArrayList<String> names = new ArrayList<>();
        for (Room room : tmp) {
            names.add(room.getName());
        }
        return names;
    }

    /**
     * Zwraca listę wszystkich termometrów
     * 
     * @return
     */
    public ArrayList<Termometr> getAllTermometers() {// TODO pomyśleć jak można to usprawnić np. przez robienie od razu
                                                     // takiej listy w momencie dodawania termometrów do systemu
        ArrayList<Termometr> termometry = new ArrayList<>();
        for (Room room : this.getRoomsArrayList()) {
            for (Sensor termometr : room.getSensors()) {
                if (termometr.getTyp() == SensorsTypes.THERMOMETR)
                    termometry.add((Termometr) termometr);
            }
        }
        return termometry;
    }

    public ArrayList<Device> getDevices() {
        return this.devices;
    }

    public String getDevicesJSON(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            return objectMapper.writeValueAsString(this.devices);
        } catch (Exception e) {
            logger.error("Błąd podczas zapisywania urządzeń do JSON", e);
            return "[]";
        }

    }

    public ArrayList<Sensor> getSensors() {
        return this.sensors;
    }

    public Sensor getSensorByOnSlaveID(int slaveAdress, int onSlaveId) {
        for (Sensor sensor : this.getSensors()) {
            if (sensor.getOnSlaveID() == onSlaveId && sensor.getSlaveAdress() == slaveAdress) {
                return sensor;
            }
        }
        return null;
    }

    public ArrayList<Button> getAllButtons() {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Room room : this.getRoomsArrayList()) {
            for (Sensor przycisk : room.getSensors()) {
                if (przycisk.getTyp() == SensorsTypes.BUTTON)
                    buttons.add((Button) przycisk);
            }
        }
        return buttons;
    }

    /**
     * Dodaje urządzenie do wskazanego pokoju
     * 
     * @param name - klucz pokoju / nazwa w systemie
     * @param d    - urządzenie do dodania
     * @throws Exception
     */
    public void addDeviceToRoom(String name, Device d) throws IllegalArgumentException {
        this.pokoje.get(name).addDevice(d);
        save(this.pokoje.get(name));
        logger.debug("Dodano urzadzenie do pokoju i zapisano");
    }

    public void addSensorToRoom(String name, Sensor s) throws IllegalArgumentException {
        this.pokoje.get(name).addSensor(s);
        save(this.pokoje.get(name));
        logger.debug("Dodano sensor do pokoju i zapisano");
    }

    /**
     * 
     * @return true jeśli w systemie zarejestrowane są pokoje/pokój
     */
    public boolean haveAnyRoom() {
        if (pokoje != null && !pokoje.isEmpty())
            return true;
        else
            return false;
    }

    /**
     * Czyta bazę danych z plików
     */
    public void readDatabase() {
        ObjectMapper obj = new ObjectMapper();
        obj.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        int i = 0;
        File f = new File(ROOMS_FILES_LOCALISATION);
        logger.debug("Lokalizacja plików z pokojami: {}", f.getAbsolutePath());
        while (i < Integer.MAX_VALUE) {
            Room room = new Room(i, "Brak");
            try {
                JsonNode jsonNode = obj.readTree(new File(ROOMS_FILES_LOCALISATION + i + "_Room.json"));
                room.setNazwa(jsonNode.get("nazwa").asText());
                for (JsonNode jsonNode2 : jsonNode.get("devices")) {
                    Device device = hardwareFactory.createDevice(DeviceTypes.valueOf( jsonNode2.get("typ").asText()));
                    device.setId(jsonNode2.get("id").asInt());
                    device.setSlaveID(jsonNode2.get("slaveID").asInt());
                    device.setOnSlaveID(jsonNode2.get("onSlaveID").asInt());
                    device.setRoom(jsonNode2.get("room").asInt());
                    device.setName(jsonNode2.get("name").asText());
                    switch (device.getTyp()) {
                        case LIGHT:
                        Light light = (Light) device;
                        light.setPin(jsonNode2.get("pin").asInt());
                        light.setState(DeviceState.valueOf(jsonNode2.get("state").asText()));
                            
                            break;
                        case BLIND:
                        Blind blind = (Blind) device;
                        blind.setPinUp(jsonNode2.get("switchUp").get("pin").asInt());
                        blind.setPinDown(jsonNode2.get("switchDown").get("pin").asInt());
                        blind.setState(DeviceState.valueOf(jsonNode2.get("state").asText()));
                            break;
                        case WENTYLATOR:
                        Fan fan = (Fan) device;
                        fan.setPin(jsonNode2.get("pin").asInt());
                        fan.setState(DeviceState.valueOf(jsonNode2.get("state").asText()));

                            break;
                        case GNIAZDKO:
                        Outlet outlet = (Outlet) device;
                        outlet.setPin(jsonNode2.get("pin").asInt());
                        outlet.setState(DeviceState.valueOf(jsonNode2.get("state").asText()));

                            break;
                        default:
                            break;
                    }

                    room.addDevice(device);
                    devices.add(device);
                }
                for (JsonNode jsonNode2 : jsonNode.get("sensors")) {
                    Sensor sensor = hardwareFactory.createSensor(SensorsTypes.valueOf( jsonNode2.get("typ").asText()));
                    sensor.setId(jsonNode2.get("id").asInt());
                    sensor.setOnSlaveID(jsonNode2.get("onSlaveID").asInt());
                    sensor.setRoom(jsonNode2.get("room").asInt());
                    sensor.setName(jsonNode2.get("name").asText());
                    switch (sensor.getTyp()) {
                        case BUTTON:
                            Button button = (Button) sensor;
                            button.setPin(jsonNode2.get("pin").asInt());
                            button.setOnSlaveID(jsonNode2.get("onSlaveID").asInt());
                            button.setSlaveAdress(jsonNode2.get("slaveAdress").asInt());
                            for (JsonNode funkcjeKlikniecJson : jsonNode2.get("funkcjeKlikniec")) {
                                ButtonLocalFunction funkcjaKlikniec = new ButtonLocalFunction();
                                funkcjaKlikniec.setButton(button);
                                funkcjaKlikniec.setClicks(funkcjeKlikniecJson.get("clicks").asInt());
                                funkcjaKlikniec.setType(ButtonClickType.valueOf(funkcjeKlikniecJson.get("type").asText()));
                                funkcjaKlikniec.setDevice(getDeviceByID(funkcjeKlikniecJson.get("device").get("id").asInt()));
                                funkcjaKlikniec.setState(ButtonLocalFunction.State.valueOf(funkcjeKlikniecJson.get("state").asText()));
                                button.addFunkcjaKilkniecia(funkcjaKlikniec);
                            }
                            break;
                        case THERMOMETR:
                            Termometr termometr = (Termometr) sensor;
                            int[] addres = new int[8];
                            int j =0;
                            for (JsonNode adrJsonNode : jsonNode2.get("addres")) {
                                addres[j++] = adrJsonNode.asInt();
                            }
                            termometr.setAddres(addres);
                            termometr.setTemperatura( (float) jsonNode2.get("temperatura").asDouble());
                            termometr.setMax((float) jsonNode2.get("max").asDouble());
                            termometr.setMin((float) jsonNode2.get("min").asDouble());
                            break;
                        //TODO dodać pozostałe sensory
                        default:
                            break;
                    }

                    room.addSensor(sensor);
                    // sensors.add(sensor);
                }

                logger.debug("Wczytano pokój: {}", jsonNode);
                pokoje.put(room.getName(), room);
                sensors.addAll(room.getSensors());
                i++;
            } catch (JsonGenerationException | JsonMappingException e) {
                logger.error("Błąd podczas wczytywania pokoi", e);
                break;
            }
            catch (IOException e) {
                logger.info("Wczytano {} pokoi", i);

                break;
            } catch (Exception e) {
                logger.error("Błąd podczas wczytywania pokoi", e);
                break;
            }
        }
    }

    /**
     * Zapisuje całą bazę danych do plików
     */
    public void save() {
        for (Room pokoj : this.getRoomsArrayList()) {
            this.save(pokoj);
        }
    }

    /**
     * Zapisuje podany pokoj do pliku
     * 
     * @param pokoj - pokoj do zapisania
     */
    public boolean save(Room pokoj) {
        if (pokoj != null) {

            try {// tworzenie plików w plikach src
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                File projFile = new File(ROOMS_FILES_LOCALISATION + pokoj.getID() + "_Room.json");
                projFile.getParentFile().mkdirs();
                projFile.createNewFile();// utworzenie pliku jeśli nie istnieje
                objectMapper.writeValue(projFile, pokoj);// plik projektu (src)
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // TODO Czy potrzebne???
            // try {//towrzenie plików w plikach programu

            // ObjectMapper objectMapper = new ObjectMapper();
            // objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            // File appFile = new
            // File(TypeReference.class.getResource("/static/database/rooms/").getPath()
            // + pokoj.getID() + "_Room.json");

            // appFile.getParentFile().mkdirs();
            // appFile.createNewFile();// utworzenie pliku jeśli nie istnieje
            // objectMapper.writeValue(appFile, pokoj);// plik aplikacji (target)
            // } catch (JsonGenerationException e) {
            // e.printStackTrace();
            // } catch (JsonMappingException e) {
            // e.printStackTrace();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Zapisuje podany pokoj do pliku
     * 
     * @param pokoj - pokoj do zapisania
     */
    public boolean delete(Room pokoj) {
        if (pokoj != null) {

            try {// tworzenie plików w plikach src
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                File projFile = new File(ROOMS_FILES_LOCALISATION + pokoj.getID() + "_Room.json");
                projFile.getParentFile().mkdirs();
                projFile.delete();// usuń plik
            } catch (Exception e) {
                e.printStackTrace();
            }
            // TODO Czy potrzebne???
            // try {//towrzenie plików w plikach programu

            // ObjectMapper objectMapper = new ObjectMapper();
            // objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            // File appFile = new
            // File(TypeReference.class.getResource("/static/database/rooms/").getPath()
            // + pokoj.getID() + "_Room.json");

            // appFile.getParentFile().mkdirs();
            // appFile.createNewFile();// utworzenie pliku jeśli nie istnieje
            // objectMapper.writeValue(appFile, pokoj);// plik aplikacji (target)
            // } catch (JsonGenerationException e) {
            // e.printStackTrace();
            // } catch (JsonMappingException e) {
            // e.printStackTrace();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Zwraca listę zawierającą urządzenia przypisane do slave-a o podanym id
     * 
     * @param id - slaveId
     * @return lista urzdządzeń
     */
    public List<Device> getAllDevicesFromSlave(int id) {
        ArrayList<Device> list = new ArrayList<>();

        for (Device device : devices) {
            if (device.getSlaveID() == id) {
                list.add(device);
            }
        }

        return list;
    }

    public Device getDeviceByID(int id) {
        for (Device device : this.getDevices()) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }

    public Sensor getSensorByID(int id) {
        for (Sensor sensor : this.getSensors()) {
            if (sensor.getId() == id) {
                return sensor;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "{" + '\n' + " pokoje='" + pokoje + "'" + "\n\n" + ", devices='" + devices + "'" + "\n\n" + ", sensors='"
                + sensors + "'" + "\n\n}";
    }

}