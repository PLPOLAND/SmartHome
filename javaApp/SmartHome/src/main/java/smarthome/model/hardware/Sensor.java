package smarthome.model.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = Termometr.class, name = "Termometr")
    })
public abstract class Sensor {
        /** Logger Springa */
    @JsonIgnore
    Logger logger;

    /** Id urządzenia w systemie */
    private int id; 
    /** ID Pokoju w, którym jest urządzenie */
    private int room;
    /** adres slave'a*/
    private int slaveID;
    /** adres urzadzenia */
    private int[] addres;//int ponieważ w arduino byte jest 0-255 a w javie -128-127
    /** ID kolejnego urządzenia w systemie */
    protected static int nextDeviceID = 0;

    SensorsTypes typ;
    
    public Sensor() {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.addres = null;
        this.typ = SensorsTypes.NONE;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info("Stworzono pusty Sensor");
    }
    public Sensor(SensorsTypes type) {
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = -1;
        this.addres = null;
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info("Stworzono Sensor:" + this.toString());
    }

    public Sensor(int slaveID, SensorsTypes type){
        this.id = nextDeviceID++;
        this.room = -1;
        this.slaveID = slaveID;
        this.addres = null;
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info("Stworzono Sensor:" + this.toString());
    }
    
    public Sensor(int id, int room, int slaveID, byte[] addres,SensorsTypes type){
        this.id = id;
        this.room = room;
        this.slaveID = slaveID;
        setAddres(addres);
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info("Stworzono Sensor:" + this.toString());
    }


    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
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

    public void setSlaveID(int slaveID) {
        this.slaveID = slaveID;
    }

    public int[] getAddres() {
        return this.addres;
    }

    public void setAddres(byte[] _addres) {
        this.addres = new int[8];

        for (int i = 0; i < 8; i++) {
            this.addres[i] = _addres[i] & 0xFF;
        }

    }

    public SensorsTypes getTyp() {
        return this.typ;
    }

    public void setTyp(SensorsTypes typ) {
        this.typ = typ;
    }

    @Override
    public String toString() {

        String tmp =""; 
        if (addres!=null) {
            for(int adres:addres){
                tmp+=adres+" ";
            }
        }

        return "{" +
            ", id='" + getId() + "'" +
            ", room='" + getRoom() + "'" +
            ", slaveID='" + getSlaveID() + "'" +
            ", addres='" + tmp + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }
    
}

   
