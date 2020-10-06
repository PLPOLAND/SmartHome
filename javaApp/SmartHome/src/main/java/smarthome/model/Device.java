package smarthome.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Device
 * 
 * @author Marek Pałdyna
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = Gniazdko.class, name = "Gniazdko"),
    @JsonSubTypes.Type(value = Light.class, name = "Light"),
    @JsonSubTypes.Type(value = Termometr.class, name = "Termometr")
    })
public abstract class Device {
    /** Logger Springa */
    Logger logger;

    /** Id urządzenia w systemie */
    int id; 
    /** ID Pokoju w, którym jest urządzenie */
    int room;
    /** ID urzadzenia na płytce drukowanej */
    int IDPlytki;
    /** ID kolejnego urządzenia w systemie */
    static int deviceId = 0;

    /** Numer pinu na płytce */
    int pin;
    

    DeviceTypes typ;
    
    public Device() {
        this.id = deviceId++;
        this.room = -1;
        this.IDPlytki = -1;
        this.pin = -1;
        this.typ = DeviceTypes.NONE;
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono Device:" + this.toString());
    }
    public Device(DeviceTypes type) {
        this.id = deviceId++;
        this.room = -1;
        this.IDPlytki = -1;
        this.pin = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono Device:" + this.toString());
    }
    public Device(DeviceTypes type, int pin) {
        if (pin<2 || pin > 13) {//TODO Poprawić górną granicę przedziału 
            throw new IllegalArgumentException("Wartość pinu po za granicami");
        }
        this.id = deviceId++;
        this.room = -1;
        this.IDPlytki = -1;
        this.pin = pin;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono Device:" + this.toString());
    }
    
    public Device(int id, int room, int roomID,DeviceTypes type, int pin){
        if (pin < 2 || pin > 13) {// TODO Poprawić górną granicę przedziału
            throw new IllegalArgumentException("Wartość pinu po za granicami");
        }
        this.id = id;
        this.room = room;
        this.IDPlytki = roomID;
        this.typ = type;
        this.pin = pin;
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono Device:" + this.toString());
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoom() {
        return this.room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public int getIDPlytki() {
        return this.IDPlytki;
    }

    public void setIDPlytki(int IDPlytki) {
        this.IDPlytki = IDPlytki;
    }

    public DeviceTypes getTyp() {
        return this.typ;
    }

    public void setTyp(DeviceTypes typ) {
        this.typ = typ;
    }

    public int getPin() {
        return this.pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", room='" + getRoom() + "'" +
            ", IDPlytki='" + getIDPlytki() + "'" +
            ", pin='" + getPin() + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }
    
}

