package newsmarthome.model.hardware.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import newsmarthome.i2c.I2C;
import newsmarthome.i2c.I2CHardware;
import smarthome.exception.HardwareException;

/**
 * Device
 * 
 * @author Marek Pałdyna
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = Switch.class, name = "Switch"),
    @JsonSubTypes.Type(value = Fan.class, name = "Light"),
    @JsonSubTypes.Type(value = Blind.class, name = "Blind")
    })
@Component
public abstract class Device {//TODO Dodać metody do parametru name.
    private static final String NOT_IMPLEMENTED_HERE = "Wywołano funkcję nie implementowaną w klasie bazowej Device!";

    /** [S, D] */
    final byte[] SPRAWDZ_STAN_URZADZENIA = { 'S', 'D' };

    @Autowired
    public I2CHardware i2c;

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
    
    private String name = "";

    DeviceTypes typ;
    // @Autowired
    protected Device( ) {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.onSlaveID = -1;
        this.typ = DeviceTypes.NONE;
        logger = LoggerFactory.getLogger(Device.class);
        name = "Undefined";
        // logger.info("Stworzono pusty Device");
    }
    protected Device(DeviceTypes type) {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.onSlaveID = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
        // logger.info("Stworzono Device:" + this.toString());
        
    }

    protected Device(int slaveID, DeviceTypes type){
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = slaveID;
        this.onSlaveID = -1;
        this.typ = type;
        logger = LoggerFactory.getLogger(Device.class);
    }
    
    protected Device(int id, int room, int slaveID,DeviceTypes type){
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


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sprawdza stan urządzenia na slavie i ustawia go w obiekcie.
     */
    public void updateDeviceState(){

        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : SPRAWDZ_STAN_URZADZENIA) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) this.slaveID;

        try {
            i2c.write(this.slaveID, buffor, 3);
            byte[] response = i2c.read(slaveID, 8);//
            
            if (response == null || response[0] == 'E' ) {
                throw new HardwareException("Error on checking state of device slaveID = " + slaveID);
            } else {
                DeviceState newState = DeviceState.NOTKNOW;
                switch (response[0]) {
                    case 'U':
                        newState = DeviceState.UP;
                        break;
                    case 'D':
                        newState = DeviceState.DOWN;
                        break;
                    case 0:
                        newState = DeviceState.OFF;
                        break;
                    case 1:
                        newState = DeviceState.ON;
                        break;
                    default:
                        newState = DeviceState.NOTKNOW;
                        break;
                }
                if(this.isStateCorrect(newState)){
                    this.changeState(newState);
                }
                else{
                    // logger.error("Otrzymano nieprawidłowy stan urządzenia. Otrzymany stan = {}", newState);
                    throw new HardwareException("Otrzymano nieprawidłowy stan urządzenia. Otrzymany stan = " + newState);
                }


            }
        } catch (HardwareException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Wysyła komendę dodającą urządzenie na slave'a 
     */
    public abstract void configureToSlave();

    /**
     * Sprawdza czy stan podany w argumencie jest zgodny z stanami dozwolonymi dla urządzenia.
     * @param state
     * @return
     */
    public abstract boolean isStateCorrect(DeviceState state);

    /**
     * Zwraca akutalny stan urządzenia
     * @return aktualny stan urządzenia
     */
    public abstract DeviceState getState();

    /**
     * Ustawia stan urządzenia na stan podany w argumencie
     * @param state - stan do ustawienia
     */
    public abstract void changeState(DeviceState state);
    /**
     * Zmienia stan urządzenia na przeciwny niż jest ustawiony w momencie wywołania funkcji
     */
    public abstract void changeState();

    /**
     * Zmienia stan urządzenia na przeciwny niż jest podany w argumencie
     */
    public abstract void changeToOppositeState(DeviceState state);

    @Override
    public String toString() {
        return "{" +
            ", id='" + getId() + "'" +
            ", room='" + getRoom() + "'" +
            ", slaveID='" + getSlaveID() + "'" +
            ", onSlaveID='" + getOnSlaveID() + "'" +
            ", name='" + getName() + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }

    
    
}

