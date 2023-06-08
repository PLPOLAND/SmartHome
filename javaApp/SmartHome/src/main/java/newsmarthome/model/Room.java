package newsmarthome.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.sensor.Sensor;



public class Room {
    /** Logger Springa */
    Logger logger;

    /** ID pokoju w systemie */
    int id;
    /** nazwa pokoju */
    String name;

    List<Device> devices;// urządzenia w pokoju
    List<Sensor> sensors;// sensory w pokoju

    public Room() {
        logger = LoggerFactory.getLogger(Room.class);
        // logger.info("Stworzono nowy pokój: {}",this);
    }

    public Room(int id, String nazwa) {
        this.id = id;
        this.name = nazwa;
        this.devices = new ArrayList<>();
        this.sensors = new ArrayList<>();
        logger = LoggerFactory.getLogger(Room.class);
        // logger.info("Stworzono nowy pokój: {}", this);
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
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

    public void addDevice(Device device) throws IllegalArgumentException {
        if (device.getId() < 0) {
            throw new IllegalArgumentException("Błędne ID urządzenia");
        }
        
        device.setRoom(this.id);// ustaw id tego pokoju w urządzeniu
        devices.add(device);
        logger.info("Dodano urządzenie:{}", device);
    }

    public void delDevice(Device urz) throws IllegalArgumentException {
        if (urz.getRoom() != this.id) {
            throw new IllegalArgumentException("Podane urządzenie nie należy do tego pokoju");
        }
        devices.remove(urz);
    }

    public void addSensor(Sensor sens) throws IllegalArgumentException {
        if (sens.getId() < 0) {
            throw new IllegalArgumentException("Błędne ID urządzenia");
        }
        sens.setRoom(this.getID());
        sensors.add(sens);
    }

    public void delSensor(Sensor sens) throws IllegalArgumentException {
        if (sens.getRoom() != this.id) {
            throw new IllegalArgumentException("Podane urządzenie nie należy do tego pokoju");
        }
        sensors.remove(sens);
    }

    public Device getDeviceById(int id) {
        for (Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }

    public void safeDelete() {
        for (Device device : devices) {

            // TODO usuwanie urządzeń z slave-a
        }
        for (Sensor sensor : sensors) {
            // TODO usuwanie sensorów z slave-a
        }
    }

    @Override
    public String toString() {
        return "{" +
                "ID='" + getID() + "'" +
                ", nazwa='" + getNazwa() + "'" +
                ", devices='" + getDevices() + "'" +
                ", sensors='" + (ArrayList<Sensor>) getSensors() + "'" +
                "}";
    }

}
