package smarthome.system;


import java.util.Arrays;

import com.pi4j.io.i2c.I2CDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.database.TemperatureDAO;
import smarthome.i2c.JtAConverter;
import smarthome.model.Room;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Sensor;
import smarthome.model.hardware.SensorsTypes;
import smarthome.model.hardware.Switch;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Termometr;
import smarthome.model.hardware.Blind.RoletaStan;

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
    JtAConverter arduino;

    Logger log;

    System(){
        log = LoggerFactory.getLogger(System.class);        
    }



    public SystemDAO getSystemDAO() {
        return this.systemDAO;
    }


    public JtAConverter getArduino() {
        return this.arduino;
    }


    /**
     * Dodaj "żarówkę" do systemu
     * 
     */
    //TODO dodać javadoc
    public Device addLight(String roomName, int BoardID, int pin) {
        Room room = systemDAO.getRoom(roomName);
        if(room == null){
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Light light = new Light(false,pin,BoardID);
        try {
            light.setOnSlaveID(arduino.addUrzadzenie(light));//dodaj urzadzenie do slavea i zapisz jego id w slavie
            if(light.getOnSlaveID()==-1){
                throw new Exception("Nie udało się dodać urządzenia na slavie");
            }
            systemDAO.getRoom(roomName).addDevice(light);
            systemDAO.getDevices().add(light);
            systemDAO.save();
        } catch (Exception e) {
            light = null;
            e.printStackTrace();
        }
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
    public Device addRoleta(String roomName, int boardID, int pinUp, int pinDown){
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Blind roleta = new Blind(false, boardID, pinUp, pinDown);
        try {
            roleta.setOnSlaveID(arduino.addUrzadzenie(roleta));// dodaj urzadzenie do slavea i zapisz jego id w slavie
            if (roleta.getOnSlaveID() == -1) {
                throw new Exception("Nie udało się dodać urządzenia na slavie");
            }
            systemDAO.getRoom(roomName).addDevice(roleta);
            systemDAO.getDevices().add(roleta);
            systemDAO.save();
        } catch (Exception e) {
            roleta = null;
            log.error(e.getMessage(), e);
        }
        return roleta;
    }
    /**
     * Dodaj "Termometr" do systemu
     * @return 
     * 
     */
    // TODO dodać javadoc
    public Termometr addTermometr(String roomName, int idPlytki) {
        Room room = systemDAO.getRoom(roomName);
        if(room == null){
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Termometr termometr = new Termometr(idPlytki);
        try {
            termometr.setAddres(arduino.addTermometr(termometr));// dodaj urzadzenie do slavea i zapisz jego id w slavie
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
    }



    public Device changeLightState(String roomName, int deviceID, boolean stan ) {

        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Light sw = (Light) room.getDeviceById(deviceID);
        if (sw != null) {
            sw.setStan(stan);
            systemDAO.save();
            arduino.changeSwitchState(sw.getOnSlaveID(), sw.getSlaveID(), sw.getStan());
        }

        
        return sw;

    }
    public Device changeLightState(int deviceID, boolean stan ) {
        Light lt = null;
        for(Device dev : systemDAO.getDevices()){
            if (dev.getId() == deviceID ) {
               if (dev instanceof Light) {
                   lt =(Light) dev; 
               }
            }
        }
        if (lt != null) {
            lt.setStan(stan);
            systemDAO.save();
            arduino.changeSwitchState(lt.getOnSlaveID(), lt.getSlaveID(), lt.getStan());
        }

        return lt;

    }

    public Device changeBlindState(String roomName, int deviceID, boolean pozycja){
        Room room = systemDAO.getRoom(roomName);
        if (room == null) {
            log.error("Nie znaleziono podanego pokoju", new Exception("Bledna nazwa pokoju"));
            return null;
        }
        Blind bl = (Blind) room.getDeviceById(deviceID);
        
        log.debug("Zmieniam pozycje rolety na: " + (pozycja?"UP":"DOWN"));
        log.debug("Aktualna pozycja rolety:" + (bl.getStan()==RoletaStan.UP?"UP":"DOWN"));
        bl.changeState(pozycja);
        log.debug("Pozycja rolety po zmianie:" + (bl.getStan()==RoletaStan.UP?"UP":"DOWN"));
        if (bl.getStan() == RoletaStan.UP) {
            arduino.changeBlindState(bl, true);
        }
        else if (bl.getStan() == RoletaStan.DOWN){
            arduino.changeBlindState(bl, false);
        }

        return bl;
    }
    /**
     * Sprawdza czy płytka była inicjowana przez I2C, i jeśli nie to reinicjuje ją
     * @param slaveID
     * @return true jeśli reinicjowano urządzenie
     * @return false jeśli urządzenie było już inicjowane 
     */
    public boolean checkInitOfBoard(int slaveID) {
        if (!arduino.checkInitOfBoard(slaveID) && arduino.reInitBoard(slaveID)) {
            log.debug("into ifs");
            log.debug("number of devices in system: {}", systemDAO.getDevices().size());
            for (Device device : systemDAO.getDevices()) {
                log.debug("device slaveID: {}",device.getSlaveID());
                if (device.getSlaveID() == slaveID) {
                    log.debug("sendingDevice {}", device);
                    device.setOnSlaveID(arduino.addUrzadzenie(device));
                    this.changeLightState(device.getId(), ((Light) device).getStan());//TODO sprawdzanie czy device jest oblektem typu light
                }
            }
            for (Sensor sensor : systemDAO.getSensors()) {
                if(sensor.getSlaveID() == slaveID){
                    if (sensor instanceof Termometr) {
                        arduino.addTermometr(sensor);//TODO sprawdzanie czy termometr po dodaniu ponownie ma taki sam adres!
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void reinitAllBoards() {
        for (I2CDevice device : arduino.atmega.getDevices()) {
            this.checkInitOfBoard(device.getAddress());
        }
    }
    /**
     * Reinicjalizuje płytke
     * @param slaveID
     * @return true jeśli reinicjowano urządzenie
     * @return false jeśli urządzenie było już inicjowane 
     */
    public boolean initOfBoard(int slaveID) {
        log.debug("initOfBoard");
        if (arduino.reInitBoard(slaveID)) {
            log.debug("into ifs");
            log.debug("number of devices in system: {}", systemDAO.getDevices().size());
            for (Device device : systemDAO.getDevices()) {
                log.debug("device slaveID: {}",device.getSlaveID());
                if (device.getSlaveID() == slaveID) {
                    log.debug("sendingDevice {}", device);
                    device.setOnSlaveID(arduino.addUrzadzenie(device));
                    this.changeLightState(device.getId(), ((Light)device).getStan());//TODO sprawdzanie czy device jest oblektem typu light
                }
            }
            for (Sensor sensor : systemDAO.getSensors()) {
                if(sensor.getSlaveID() == slaveID){
                    if (sensor instanceof Termometr) {
                        arduino.addTermometr(sensor);//TODO sprawdzanie czy termometr po dodaniu ponownie ma taki sam adres!
                    }
                }
            }
            return true;
        }
        return false;
    }
}
