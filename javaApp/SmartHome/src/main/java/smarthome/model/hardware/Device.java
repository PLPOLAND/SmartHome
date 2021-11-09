package smarthome.model.hardware;

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
    @JsonSubTypes.Type(value = Switch.class, name = "Switch"),
    @JsonSubTypes.Type(value = Light.class, name = "Light"),
    @JsonSubTypes.Type(value = Roleta.class, name = "Blind")
    })
public abstract class Device {
    /** Logger Springa */
    Logger logger;

    /** Id urządzenia w systemie */
    private int id; 
    /** ID Pokoju w, którym jest urządzenie */
    private int room;
    /** ID urzadzenia na płytce drukowanej */
    private int slave_ID;
    /** ID kolejnego urządzenia w systemie */
    protected static int nextDeviceID = 0;
    

    DeviceTypes typ;
    
    public Device() {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slave_ID = -1;
        this.typ = DeviceTypes.NONE;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono pusty Device");
    }
    public Device(DeviceTypes type) {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slave_ID = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono Device:" + this.toString());
    }
    
    public Device(int id, int room, int roomID,DeviceTypes type){
        this.id = id;
        this.room = room;
        this.slave_ID = roomID;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono Device:" + this.toString());
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
        return this.slave_ID;
    }
    
    public void setIDPlytki(int idPlytki) {
        this.slave_ID = idPlytki;
    }
    
    public DeviceTypes getTyp() {
        return this.typ;
    }
    // public byte getTypAsByte() {
    //     switch (typ) {
    //         case PRZEKAZNIK:
    //             return 0;
    //         case SWIATLO:
    //             return 1;
    //         case TERMOMETR:
    //             return 2;
    //         default:
    //             return -1;
    //     }
    // }

    public void setTyp(DeviceTypes typ) {
        this.typ = typ;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", room='" + getRoom() + "'" +
            ", IDPlytki='" + getIDPlytki() + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }
}

