package smarthome.system;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.pi4j.io.i2c.I2CDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.automation.ButtonFunction;
import smarthome.automation.Function;
import smarthome.automation.FunctionAction;
import smarthome.database.AutomationDAO;
import smarthome.database.SystemDAO;
import smarthome.database.TemperatureDAO;
import smarthome.exception.HardwareException;
import smarthome.exception.SoftwareException;
import smarthome.i2c.MasterToSlaveConverter;
import smarthome.model.Room;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.DeviceState;
import smarthome.model.hardware.Sensor;
import smarthome.model.hardware.SensorsTypes;
import smarthome.model.hardware.Switch;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonClickType;
import smarthome.model.hardware.ButtonLocalFunction;
import smarthome.model.hardware.Termometr;
import smarthome.runners.Automation;
import smarthome.runners.Runners;

/**
 * Główna klasa zarządzająca systemem
 * @author Marek Pałdyna
 */

@Service
public class System {

    @Autowired
    SystemDAO systemDAO;
    @Autowired
    TemperatureDAO temperatureDAO;
    @Autowired
    MasterToSlaveConverter arduino;

    @Autowired
    AutomationDAO automationDAO;

    Logger log;

    private System(){
        log = LoggerFactory.getLogger(System.class);
    }

    public SystemDAO getSystemDAO() {
        return this.systemDAO;
    }

    public AutomationDAO getAutomationDAO() {
        return this.automationDAO;
    }

    public MasterToSlaveConverter getArduino() {
        return this.arduino;
    }

    public Device getDeviceByID(int id) {
        return systemDAO.getDeviceByID(id);
    }
    public Sensor getSensorByID(int id) {
        for (Sensor device : systemDAO.getSensors()) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }
    public Sensor getSensorByOnSlaveID(int slaveAdress, int onSlaveId) {
        for (Sensor sensor : systemDAO.getSensors()) {
            if (sensor.getOnSlaveID() == onSlaveId && sensor.getSlaveAdress() == slaveAdress) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Dodaj "żarówkę" do systemu
     * 
     */
    //TODO dodać javadoc
    public Device addLight(String roomName, String name, int boardID, int pin) throws IllegalArgumentException, HardwareException {
        Room room = systemDAO.getRoom(roomName);
        if(room == null){
            log.error("Nie znaleziono pokoju o podanej nazwie \"{}\" podczas dodawania światła",roomName);
            throw new IllegalArgumentException("Bledna nazwa pokoju");
        }
        Light light = new Light(DeviceState.OFF,pin,boardID);
        light.setName(name);
        light.setOnSlaveID(arduino.addUrzadzenie(light));//dodaj urzadzenie do slavea i zapisz jego id w slavie
        if(light.getOnSlaveID()==-1){
            throw new HardwareException("Nie udało się dodać urządzenia na slavie");
        }

        int maxID = 0;
        for (Device device : systemDAO.getDevices()) {// znajdź maksymalne id w systemie
            if (device.getId() > maxID) {
                maxID = device.getId();
            }
        }
        light.setId(maxID+1);
        systemDAO.getRoom(roomName).addDevice(light);
        systemDAO.getDevices().add(light);
        systemDAO.save();
        log.debug("Dodano żarówkę na Slave-a o id: {}", boardID);
        return light;
    }

    /**
     * Dodaje roletę do systemu, na masterze i na slavie
     * @param roomName
     * @param boardID
     * @param pinUp
     * @param pinDown
     * @return
     */
    public Device addRoleta(String roomName, String name, int boardID, int pinUp, int pinDown) throws HardwareException, IllegalArgumentException{
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            IllegalArgumentException tmp = new IllegalArgumentException("Bledna nazwa pokoju");
            log.error("Nie znaleziono podanego pokoju", tmp);
            throw tmp;
        }
        Blind roleta = new Blind(false, boardID, pinUp, pinDown);
        roleta.setName(name);
        roleta.setOnSlaveID(arduino.addUrzadzenie(roleta));// dodaj urzadzenie do slavea i zapisz jego id w slavie
        if (roleta.getOnSlaveID() == -1) {
            throw new HardwareException("Nie udało się dodać urządzenia na slavie");
        }
        int maxID = 0;
        for (Device device : systemDAO.getDevices()) {// znajdź maksymalne id w systemie
            if (device.getId() > maxID) {
                maxID = device.getId();
            }
        }
        roleta.setId(maxID+1);
        systemDAO.getRoom(roomName).addDevice(roleta);
        systemDAO.getDevices().add(roleta);
        systemDAO.save();
        
        log.debug("Dodano roletę na Slave-ie o id: {}", boardID);

        return roleta;
    }

    /**
     * Dodaj "Przycisk" do systemu
     * 
     * @return
     * 
     */
    // TODO dodać javadoc
    public Button addButton(String name, String roomName, int boardID, int pin) throws IllegalArgumentException, HardwareException{
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            IllegalArgumentException tmp = new IllegalArgumentException("Bledna nazwa pokoju");
            log.error("Nie znaleziono podanego pokoju", tmp);
            throw tmp;
        }
        Button button = new Button(boardID, pin);
        button.setName(name);
        button.setOnSlaveID(arduino.addPrzycisk(button));// dodaj urzadzenie do slavea i zapisz jego id w slavie
        if (button.getOnSlaveID() == -1) {
            throw new HardwareException("Nie udało się dodać urządzenia na slavie");
        }
        int maxID = 0;
        for (Sensor sensor : systemDAO.getSensors()) {//znajdź maksymalne id w systemie
            if (sensor.getId()>maxID) {
                maxID= sensor.getId();
            }
        }
        button.setId(maxID+1);
        systemDAO.getRoom(roomName).addSensor(button);
        systemDAO.getSensors().add(button);
        systemDAO.save();
        log.debug("Przycisk został dodany na płytkę o id: {}", boardID);
        return button;
    }

    //TODO dodać javaDoc
    public void removeDevice(Device device, Room room){
        ArrayList<Button> buttons = systemDAO.getAllButtons();
        ArrayList<ButtonLocalFunction> toRemove = new ArrayList<>();
        for (Button button : buttons) {
            for (ButtonLocalFunction buttonFunction : button.getFunkcjeKlikniec()) {
                if (buttonFunction.getDevice().equals(device)) {
                    toRemove.add(buttonFunction);
                    
                }
            }
        }
        for (ButtonLocalFunction buttonFunction : toRemove) {
            Button tmp = buttonFunction.getButton();
            tmp.removeFunkcjaKilkniecia(buttonFunction.getClicks());
        }

        room.delDevice(device);
        systemDAO.getDevices().remove(device);
        systemDAO.save(room);
        
        initOfBoard(device.getSlaveID());
        // TODO usuwanie urzadzenia z funkcji przycisków!
    }
    
    // TODO dodać javaDoc
    public void removeSensor(Sensor sen, Room room) {
        room.delSensor(sen);
        systemDAO.getSensors().remove(sen);
        systemDAO.save(room);
        initOfBoard(sen.getSlaveAdress());
    }
    /**
     * Dodaj "Termometr" do systemu
     * @deprecated obsługa wyjątków została zmieniona, zmieniono sposób dodawania termomterów na płytki i w systemie
     * @return 
     * 
     */
    // TODO dodać javadoc
    @Deprecated
    public Termometr addTermometr(String roomName, int boardID) {
        Room room = systemDAO.getRoom(roomName);
        if(room == null){
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Termometr termometr = new Termometr(boardID);
        try {
            termometr.setAddres(arduino.addTermometr(boardID));// dodaj urzadzenie do slavea i zapisz jego id w slavie
            if (termometr.getAddres() == null) {
                throw new Exception("Nie udało się dodać urządzenia na slavie");
            }
            systemDAO.getRoom(roomName).addSensor(termometr);//dodaj urzadzenie do pokoju
            systemDAO.getSensors().add(termometr);
            systemDAO.save();
        } catch (Exception e) {
            termometr = null;
            e.printStackTrace();
        }
        return termometr;
    }

    /**
     * Przeszkuje listę sensorów  w poszukianiu termometru o podanym id
     * @param roomName
     * @param idTermometru
     * @return
     */
    private Termometr getTermometr(String roomName, int idTermometru){
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        for (Sensor termometr : room.getSensors()) {
            if (termometr.getId() == idTermometru && (termometr.getTyp() == SensorsTypes.THERMOMETR || termometr.getTyp() == SensorsTypes.THERMOMETR_HYGROMETR)) {
                return (Termometr) termometr;
            }
        }
        return null;

    }
    
    /**
     * Przeszkuje listę sensorów w poszukiwaniu termometru o podanym adresie
     * 
     * @param roomName
     * @param adress
     * @return
     */
    private Termometr getTermometr(String roomName, int[] adress){
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        for (Sensor termometr : room.getSensors()) {
            if (Arrays.equals(termometr.getAddres(), adress) && (termometr.getTyp() == SensorsTypes.THERMOMETR || termometr.getTyp() == SensorsTypes.THERMOMETR_HYGROMETR)) {
                return (Termometr) termometr;
            }
        }
        return null;

    }
    
    /**
     * Przeszkuje listę sensorów w poszukiwaniu termometru o podanym adresie
     * 
     * @param roomName
     * @param adress
     * @return
     */
    private Termometr getTermometr(int[] adress) {
        for (Room room : this.systemDAO.getRoomsArrayList()) {
            Termometr tmp = getTermometr(room.getNazwa(), adress);
            if(tmp != null){
                return tmp;
            }
        }
        return null;

    }
    
    public Float getTemperature(String roomName, int[] adress){
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return -128.f;
        }

        Termometr tmp = this.getTermometr(roomName, adress);
        if (tmp!= null) {
            return tmp.getTemperatura();
        }
        else
            return -128.f;
    }
    public Float getTemperature(int[] adress){
        Termometr tmp = this.getTermometr(adress);
        if (tmp != null) {
            return tmp.getTemperatura();
        }
        return -128.f;
    }

    public void updateTemperature(Termometr termometr){
        arduino.checkTemperature(termometr);
        systemDAO.save(systemDAO.getRoom(termometr.getRoom()));
    }

    /**
     * Dodawanie funkcji lokalnej do przycisku na slavie
     * @param buttonID - id przycisku do którego zostanie dodana funkcja
     * @param deviceToControl - urządzenie które będzie kontrolowane
     * @param state - stan na który będzie przełączane kontrolowane urządzenie
     * @param numberOfClicks - ilość przyciśnięć przycisku wymaganych do wywołania danej funkcji
     * @throws HardwareException
     */
    public void addFunctionToButton(int buttonID, Device deviceToControl, ButtonLocalFunction.State state, int numberOfClicks) throws HardwareException{
        ButtonLocalFunction function = new ButtonLocalFunction(null, deviceToControl, state, numberOfClicks);
        addFunctionToButton(buttonID, function);

    }

    /**
     * Dodaje funkcję lokalną do przyciksu na slavie
     * @param buttonID - idprzycisku w systemie do którego zostanie dodana funkcja
     * @param function - funkcja do dodania
     * @return ButtonFunction 
     * @see smarthome.model.hardware.ButtonLocalFunction
     * 
     * @throws HardwareException
     */
    public ButtonLocalFunction addFunctionToButton(int buttonID, ButtonLocalFunction function)throws HardwareException{
        
        Button but = (Button) this.getSensorByID(buttonID);
        but.addFunkcjaKilkniecia(function);
        arduino.sendClickFunction(function);
        return function;
    }
    /**
     * Usuwa funkcję z systemu i slave-a
     * @param buttonID - id przycisku do którego została przypisana funkcja, która ma zostać usunięta
     * @param numberOfClicks - ilość przyciśnieć która wywołuje funkcję
     * @throws HardwareException
     */
    public void removeFunctionToButton(int buttonID, int numberOfClicks) throws HardwareException{
        Button but = (Button) this.getSensorByID(buttonID);
        but.removeFunkcjaKilkniecia(numberOfClicks);
        arduino.sendRemoveFunction(but.getSlaveAdress(),numberOfClicks);
        
    }
    
    /**
     * Usuwa funkcję z systemu i slave-a
     * 
     * @param button - przycisk do którego została przypisana funkcja, która ma zostać usunięta
     * @param numberOfClicks - ilość przyciśnieć która wywołuje funkcję
     * @throws HardwareException
     */
    public void removeFunctionFromButton(Button button, int numberOfClicks) throws HardwareException{
        button.removeFunkcjaKilkniecia(numberOfClicks);
        arduino.sendRemoveFunction(button.getSlaveAdress(),numberOfClicks);
        
    }

    /**
     * Zmienia stan światła
     * @param roomName - nazwa pokoju w którym znajduje się światło
     * @param deviceID - id urządzenia reprezentującego światło
     * @param stan - stan na jaki ma zostać przełączone światło
     * @return urządzenie, którego stan został zmieniony
     * @throws IllegalArgumentException
     * @throws HardwareException
     */
    public Device changeLightState(String roomName, int deviceID, boolean stan ) throws IllegalArgumentException, HardwareException{

        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Light sw = (Light) room.getDeviceById(deviceID);
        if (sw != null) {
            sw.setState(stan?DeviceState.ON:DeviceState.OFF);
            systemDAO.save(room);
            arduino.changeSwitchState(sw.getOnSlaveID(), sw.getSlaveID(), sw.getState());
        }

        
        return sw;

    }

    public Device changeLightState(int roomID, int deviceID, DeviceState stan ) throws IllegalArgumentException, HardwareException{

        Room room = systemDAO.getRoom(roomID);
        if (room == null) {
            throw new IllegalArgumentException("Nie znaleziono pokouj o podanym id. Id: " + roomID);
        }
        Light l = (Light) room.getDeviceById(deviceID);
        if (l != null) {
            arduino.changeSwitchState(l.getOnSlaveID(), l.getSlaveID(), stan);
            l.setState(stan);
            systemDAO.save(room);
        }

        
        return l;

    }

    public Device changeLightState(int deviceID, DeviceState stan ) throws IllegalArgumentException, HardwareException{
        Light lt = null;
        for(Device dev : systemDAO.getDevices()){
            if (dev.getId() == deviceID ) {
               if (dev instanceof Light) {
                   lt =(Light) dev; 
               }
            }
        }
        if (lt != null) {
            log.debug("Zmiana stanu Światła");
            arduino.changeSwitchState(lt.getOnSlaveID(), lt.getSlaveID(), stan);
            lt.setState(stan);
            systemDAO.save();
        }
        else
            throw new IllegalArgumentException("Błędne id urządzenia - brak urządzenia o takim id");

        return lt;

    }
    /**
     * Zmienia stan rolety
     * @param roomName - nazwa pokoju w którym znajduje się roleta
     * @param deviceID - id rolety w systemie
     * @param pozycja - pozycja jaka powinna zostać ustawiona (true == UP)
     * @return roleta
     */
    public Device changeBlindState(String roomName, int deviceID, boolean pozycja) throws HardwareException, IllegalArgumentException{
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            throw new IllegalArgumentException("Nie znaleziono podanego pokoju");
        }
        Blind bl = (Blind) room.getDeviceById(deviceID);
        log.debug("roleta: {}", bl);
        log.debug("Zmieniam pozycje rolety na: {}",(pozycja?"UP":"DOWN"));
        bl.changeState(pozycja?DeviceState.UP:DeviceState.DOWN);
        if (bl.getState() == DeviceState.UP) {
            arduino.changeBlindState(bl, DeviceState.UP);
        }
        else if (bl.getState() == DeviceState.DOWN){
            arduino.changeBlindState(bl, DeviceState.DOWN);
        }
        systemDAO.save(room);

        return bl;
    }
    
    /**
     * Wysyła konfigurację slave-a na urządzenie
     * 
     * @param slaveID  - adres slave-a na którego ma zostać wysłana konfiguracja
     * @return czy wysyłanie się powiodło
     */
    private boolean sendConfigToSlave(int slaveID) {
        boolean toReturn = true;
        log.info("Wysyłam konfigurację slave-a {}", slaveID);
        Automation.pause();//wstrzymaj sprawdzanie automatyki na czas wysyłania konfiguracji.
        Runners.pause();
        for (Device device : systemDAO.getAllDevicesFromSlave(slaveID)) {
            log.debug("sendingDevice {}", device);
            try {
                device.setOnSlaveID(arduino.addUrzadzenie(device));
                if (device instanceof Light) {
                    this.changeLightState(device.getId(), ((Light) device).getState());
                } else if (device instanceof Blind) {
                    switch (((Blind) device).getState()) {
                        case UP:
                            this.changeBlindState(systemDAO.getRoom(device.getRoom()).getNazwa(), device.getId(), true); // zmienia stan rolety na UP
                            break;
                        case DOWN:
                            this.changeBlindState(systemDAO.getRoom(device.getRoom()).getNazwa(), device.getId(), false); // zmienia stan rolety na DOWN
                            break;
                        case NOTKNOW:
                            break; // nie rob nic
                        default:
                            break;
                    }
                }
            } catch (HardwareException e) {
                log.error("Nie udało się reinicjalizować urządzenia o id {}", slaveID, e);
                toReturn = false;
            }
        }
        for (Sensor sensor : systemDAO.getSensors()) {
            if (sensor.getSlaveAdress() == slaveID) {
                log.debug("sensor id: {}", sensor.getId());
                if (sensor instanceof Button) {
                    try {
                        log.debug("sending Button: {}", sensor);
                        sensor.setOnSlaveID(arduino.addPrzycisk((Button) sensor));
                        if (!((Button) sensor).getFunkcjeKlikniec().isEmpty()) {
                            for (ButtonLocalFunction bFunction : ((Button) sensor).getFunkcjeKlikniec()) {
                                arduino.sendClickFunction(bFunction);
                            }
                        }
                    } catch (HardwareException e) {
                        log.error("Nie udało się reinicjalizować przycisku o id {}", slaveID, e);
                        toReturn = false;
                    }
                }
            }
        }

        try {
            this.addUpdateThermometersOnSlave(slaveID);
        } catch (HardwareException e) {
            log.error("Błąd podczas dodawania termometerów: '{}'", e.getMessage());
            toReturn = false;
        }
        Automation.resume();//wznow sprawdzanie automatyki.
        Runners.resume();
        return toReturn;
    }

    /**
     * Sprawdza czy płytka była inicjowana przez I2C, i jeśli nie to reinicjuje ją
     * @param slaveAdress
     * @return true jeśli reinicjowano urządzenie
     * @return false jeśli urządzenie było już inicjowane 
     */
    public boolean checkInitOfBoard(int slaveAdress) throws HardwareException, SoftwareException {
        // log.debug("Sprawdzanie slave-a o id: {}", slaveID);
        boolean toReturn = false;
        for (int i = 0; i < 10; i++) {
            
            try {
                if (!arduino.checkInitOfBoard(slaveAdress) && arduino.reInitBoard(slaveAdress)) {//Sprawdź czy płytka była inicjowana, i jeśli nie to wyślij komendę o reinicjalizacji urządzenia
                    toReturn = sendConfigToSlave(slaveAdress);
                }
                break;
            } catch (HardwareException | SoftwareException e) {
                log.error("Błąd podczas sprawdzania czy płytka była inicjowana: '{}'", e.getMessage());
                if (i == 9) {
                    throw e;
                }
            }
        }
        return toReturn;
    }


 
    public void reinitAllBoards() {
        for (I2CDevice device : arduino.atmega.getDevices()) {
            try {
                checkInitOfBoard(device.getAddress());
            } catch (HardwareException | SoftwareException e) {
                log.error("Błąd podczas reinicjalizacji płytki: '{}'", e.getMessage());
            }
        }
    }
    /**
     * Reinicjalizuje płytke
     * @param slaveID
     * @return true jeśli reinicjowano urządzenie
     * @return false jeśli urządzenie było już inicjowane 
     */
    public boolean initOfBoard(int slaveID) {
        log.info("Inicjalizacja slave-a o id: {}", slaveID);
        if (arduino.reInitBoard(slaveID)) {
            return sendConfigToSlave(slaveID);
        }
        return false;
    }


    public void updateDeviceState(Device device) throws HardwareException{
        log.debug("updating device: id = {}, type = {}, slaveID = {}",device.getId(), device.getTyp(), device.getSlaveID());
        int state = arduino.checkDeviceState(device.getSlaveID(), device.getOnSlaveID());
        if (device.getTyp() == DeviceTypes.BLIND) {
            // log.debug("BLIND");
            Blind b = (Blind) device;
            if (state == 'U') {
                b.changeState(DeviceState.UP);
                // log.debug("UP");
            }
            else if (state == 'D') {
                b.changeState(DeviceState.DOWN);
                // log.debug("DOWN");
            }
            else if (state == 'K') {//TODO: TO TEST
                b.changeState(DeviceState.NOTKNOW);
                // log.debug("NOTKNOW");
            }
        }
        else if(device.getTyp() == DeviceTypes.LIGHT || device.getTyp() == DeviceTypes.GNIAZDKO){
            if (device instanceof Light) {
                // log.debug("LIGHT");
                Light l = (Light) device;
                l.setState(state == 1 ? DeviceState.ON : DeviceState.OFF);
            } 
            // else if (device instanceof { // TODO Po dodaniu gniazdek do systemu dodać kod!
                
            // }
        }


    }

    public void addUpdateThermometersOnAllSlaves() {
        for (Integer slaveAdres : arduino.getSlavesAdresses()) { //dla każdego slave-a w systemie
            try {
                addUpdateThermometersOnSlave(slaveAdres);
            } catch (HardwareException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Automatycznie sprawdza ile termometrów jest podłączonych do danego slave-a i dodaje je. Jeśli, dodany termomter istniał już wcześniej w systemie to jego dane zostają zaktualizowane.
     * @param slaveAdres - adres slave-a, który ma zostać sprawdzony w poszukiwaniu termometrów.
     * @throws HardwareException
     */
    public void addUpdateThermometersOnSlave(Integer slaveAdres) throws HardwareException {
        ArrayList<Termometr> termometry = systemDAO.getAllTermometers();
        int ile = arduino.howManyThermometersOnSlave(slaveAdres);//sprawdź ile jest dostępnych termometrów
        arduino.atmega.setOccupied(false);
        if (ile > 0) {//jeśli jest ich więcej niż 0 
            for (int i = 0; i < ile; i++) {
                int[] addres;
                try {
                    addres = arduino.addTermometr(slaveAdres);//dodaj termometr na slavie
                    
                } catch (HardwareException e) {
                    log.error(e.getMessage());
                    throw e;
                }
                
                boolean existed = false;//czy ten termometr był już dodany w systemie

                for (Termometr t : termometry) {//wśród wszystkich dodanych w systemie 
                    if (Arrays.equals(t.getAddres(), addres)) {//znajdź ten który został właśnie dodany
                        t.setSlaveAdress(slaveAdres);//zmień mu adres slave-a
                        existed = true;
                        break;
                    }
                }
                if (!existed) {
                    Termometr termometr = new Termometr(slaveAdres);
                    termometr.setAddres(addres);
                    termometr.setName("Dodany automatycznie, slave=" + slaveAdres);
                    Room tmp = systemDAO.getRoom("Brak");
                    if (tmp != null) {
                        
                        termometr.setRoom(tmp.getID());
                        tmp.addSensor(termometr);
                        systemDAO.save(tmp);
                    }
                    else{
                        log.error("Nie znaleziono pokoju '{}' podczas dodawania nowego termometru  ","Brak");
                    }
                }
            }
        }
    }

    /**
     * Funkcja edytuje dane o termometrze i zapisuje je w pamięci
     * @param termometerId - id termometru, który będzie aktualizowany
     * @param name - nowa nazwa termometru
     * @param roomName - nowy pokój do którego będzie należał termometr
     * @throws HardwareException
     */
    public void editThermometer(int termometerId, String name, String roomName) throws HardwareException {
        
        Termometr termometr = (Termometr) this.getSensorByID(termometerId);
        if (termometr != null) {
            termometr.setName(name);
            int oldRoom = termometr.getRoom();
            termometr.setRoom(this.systemDAO.getRoom(roomName).getID());
            if (oldRoom == termometr.getRoom()) {
                systemDAO.save(systemDAO.getRoom(termometr.getRoom()));
            }
            else{
                systemDAO.getRoom(oldRoom).delSensor(termometr);
                systemDAO.getRoom(roomName).addSensor(termometr);
                systemDAO.save(systemDAO.getRoom(oldRoom));
                systemDAO.save(systemDAO.getRoom(roomName));
            }
        }
        else{
            throw new HardwareException("Brak sensora o id '"+termometerId+"'");
        }
    }
    /**
     * Sprawdza czy slave jest w systemie i czy jest podłączony.
     * @param deviceId
     * @return
     */
    public boolean isSlaveConnected(int deviceId) {
        return arduino.isDeviceConnected(deviceId);
    }

    public int checkHowManyCommandsToReadFromSlave(int slaveAdress) throws HardwareException {
        return arduino.howManyCommandToRead(slaveAdress);
    }

    public byte[] readCommandFromSlave(int slaveAdress) throws HardwareException {
        return arduino.readCommandFromSlave(slaveAdress);
    }
    
    private void executeSlaveCommand(int slaveAdress,byte[] command) throws HardwareException{
        if ( command[0] =='C') {
            ButtonFunction but = new ButtonFunction();
            but.fromCommand(slaveAdress, command); //zainicjuj funkcję z danych z slave-a
            log.debug("Pobrano z slave-a fun: {}",but);

            for (ButtonFunction fun : automationDAO.getButtonFunctions()) {
                if(fun.compare(but)){
                    log.debug("Znaleziono funkcję: {}",fun);
                    fun.run();
                    break;
                }
                
            }
            return;//TODO
            

        }
    }
    public void checkGetAndExecuteCommandsFromSlave (int slaveAdress) {
        try {
            // log.debug("Sprawdzam czy slave {} ma jakieś polecenia do wykonania",slaveAdress);
            int howMany = arduino.howManyCommandToRead(slaveAdress);
            if (howMany > 0) {
                for (int i = 0; i < howMany; i++) {
                    byte[] command = arduino.readCommandFromSlave(slaveAdress);
                    if (command != null) {
                        executeSlaveCommand(slaveAdress,command);
                    }
                }
            }
        } catch (HardwareException e) {
            log.error(e.getMessage(), e);
        } 

    }

    /**
     * Dodadaje nową funkcję globalną przycisku do systemu.
     * @param fun - funckja do dodania
     * @return - id dodanej funkcji
     * @throws HardwareException
     */
    public int addButtonAutomation(ButtonFunction fun) throws HardwareException {
        automationDAO.addFunction(fun);
        return fun.getId();
    }
    /**
     * Dodaje nową funkcję globalną przycisku do systemu.
     * @param button - przycisk skojarzony z funkcją
     * @param clicks - ilość kliknięć przycisku wywołująca funkcję
     * @param clickType - typ kliknięcia przycisku wywołującego funkcję (kliknięty, przytrzymywany, przytrzymany)
     * @return - id dodanej funkcji
     * @throws HardwareException
     */
    public int addButtonAutomation(Button button, int clicks, ButtonClickType clickType, String name) throws HardwareException {
        ButtonFunction fun = new ButtonFunction(button, clicks, clickType);
        fun.setName(name);
        return addButtonAutomation(fun);
    }

    /**
     * Usuwa funkcję globalną przycisku z systemu. 
     * @param id - id funkcji do usunięcia
     * @throws HardwareException
     */
    public void removeButtonAutomation(int id){
        automationDAO.removeFunction(id);
    }
    
    /**
     * Usuwa funkcję z systemu. 
     * @param id - id funkcji do usunięcia
     * @throws HardwareException
     */
    public void removeFunction(int id)  {
        automationDAO.removeFunction(id);
    }

    /**
     * Dodaje nową akcję do funckji o podanym id.
     * @param functionId
     * @param action
     */
    public void addActionToFunction(int functionId, FunctionAction action){
        try {
            Function f = automationDAO.getFunction(functionId);
            f.addAction(action);
        } catch (Exception e) {
           log.error("Błąd podczas dodawania akcji do funkcji: {}",e.getMessage());
        }
    }
    /**
     * Dodaje nową akcję do funckji o podanym id.
     * @param functionId
     * @param device
     * @param activeDeviceState
     * @param allowReverse
     */
    public void addActionToFunction (int functionId, Device device, DeviceState activeDeviceState, boolean allowReverse){
        try {
            Function f = automationDAO.getFunction(functionId);
            f.addAction(device,activeDeviceState,allowReverse);
        } catch (Exception e) {
           log.error("Błąd podczas dodawania akcji do funkcji: {}",e.getMessage());
        }
    }
    /**
     * Usuwa akcję z funkcji o podanym id.
     * @param functionId - id funckji z której zostanie usunięta akcja
     * @param device - urządzenie którym steruje akcja
     * @param activeDeviceState - stan urządzenia którym steruje akcja
     */
    public void removeActionFromFunction( int functionId, Device device, DeviceState activeDeviceState){
        try {
            Function f = automationDAO.getFunction(functionId);
            f.removeAction(device,activeDeviceState);
        } catch (Exception e) {
           log.error("Błąd podczas usuwania akcji z funkcji: {}",e.getMessage());
        }
    }
    /**
     * Sprawdza czy stan urządzenia podany w argumencie jest prawidłowy dla urządzenia o podanym id.
     * @param id
     * @param state
     * @return
     */
    public boolean isDeviceStateCorrectForDevice(int id, DeviceState state){
        try {
            Device d = systemDAO.getDeviceByID(id);
            return d.isStateCorrect(state);
        } catch (Exception e) {
           log.error("Błąd podczas sprawdzania stanu poprawności stanu urządzenia: {}",e.getMessage());
        }
        return false;
    }
}
