package smarthome.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Sensor;

public class Room {
    /** Logger Springa */
    Logger logger;

    /**ID pokoju w systemie */
    int ID;
    /** nazwa pokoju */
    String name;
    
    List<Device> devices;// urządzenia w pokoju
    List<Sensor> sensors;// sensory w pokoju

    public Room(){
        logger = LoggerFactory.getLogger(Room.class);
        logger.info("Stworzono nowy pokój:" + this.toString());
    }

    public Room(int ID, String nazwa) {
        this.ID = ID;
        this.name = nazwa;
        this.devices = new ArrayList<>();
        this.sensors = new ArrayList<>();
        logger = LoggerFactory.getLogger(Room.class);
        logger.info("Stworzono nowy pokój:" + this.toString());
    }

    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNazwa() {
        return this.name;
    }

    public void setNazwa(String nazwa) {
        this.name = nazwa;
    }

    public List<Device> getDevices() {
        return this.devices;
    }
    public List<Sensor> getSensors() {
        return this.sensors;
    }
    public void addDevice(Device device) throws Exception {
        if (device.getId() < 0) {
            throw new Exception("Błędne ID urządzenia");
        }
        // if (urz.roomID < 0) { //TODO po za implementowaniu pojęcia płytki odkommentować
        //     throw new Exception("Błędny adres płytki");
        // }

        device.setRoom(this.ID);//ustaw id tego pokoju w urządzeniu
        devices.add(device);
        logger.info("Dodano urządzenie:" + device.toString());
    }
    public void delDevice(Device urz) throws Exception {
        if (urz.getRoom() != this.ID) {
            throw new Exception("Podane urządzenie nie należy do tego pokoju");
        }
        devices.remove(urz);
    }

    public void addSensor(Sensor sens) throws Exception {
        if (sens.getId() < 0) {
            throw new Exception("Błędne ID urządzenia");
        }
        sens.setRoom(this.getID());
        sensors.add(sens);
    }
    
    public void delSensor(Sensor sens) throws Exception {
        if (sens.getRoom() != this.ID) {
            throw new Exception("Podane urządzenie nie należy do tego pokoju");
        }
        sensors.remove(sens);
    }
    public Device getDeviceById(int id){
        for (Device device : devices) {
            if(device.getId() == id){
                return device;
            }
        }
        return null;
    }

    public void safeDelete(){
        for (Device device : devices) {
            //TODO usuwanie urządzeń z systemu
        }
        for (Sensor sensor : sensors) {
            //TODO usuwanie sensorów z systemu
        }
    }

    @Override
    public String toString() {
        return "{" +
            "ID='" + getID() + "'" +
            ", nazwa='" + getNazwa() + "'" +
            ", devices='" + getDevices() + "'" +
            ", sensors='" + getSensors() + "'" +
            "}";
    }
    


}
