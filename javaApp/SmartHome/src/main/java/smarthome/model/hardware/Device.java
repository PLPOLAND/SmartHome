package smarthome.model.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    /** Logger Springa */
    Logger logger;

    /** Id urządzenia w systemie */
    private int id; 
    /** ID Pokoju w, którym jest urządzenie */
    private int room;
    /** ID slave'a */
    private int slaveID;
    /** ID urzadzenia na płytce drukowanej */
    private int onSlaveID;
    /** ID kolejnego urządzenia w systemie */
    protected static int nextDeviceID = 0;
    

    DeviceTypes typ;
    
    public Device() {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.onSlaveID = -1;
        this.typ = DeviceTypes.NONE;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono pusty Device");
    }
    public Device(DeviceTypes type) {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.onSlaveID = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono Device:" + this.toString());
    }

    public Device(int slaveID, DeviceTypes type){
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = slaveID;
        this.onSlaveID = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
    }
    
    public Device(int id, int room, int slaveID,DeviceTypes type){
        this.id = id;
        this.room = room;
        this.slaveID = slaveID;
        this.onSlaveID = -1;
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

    public int getSlaveID() {
        return this.slaveID;
    }
    
    public void setSlaveID(int idPlytki) {
        this.slaveID = idPlytki;
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

    public int getOnSlaveID() {
        return this.onSlaveID;
    }

    public void setOnSlaveID(int onSlaveID) {
        this.onSlaveID = onSlaveID;
    }


    @Override
    public String toString() {
        return "{" +
            ", id='" + getId() + "'" +
            ", room='" + getRoom() + "'" +
            ", slave_ID='" + getSlaveID() + "'" +
            ", onSlaveID='" + getOnSlaveID() + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }
    
}

