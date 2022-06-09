package smarthome.model.hardware;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = Termometr.class, name = "Termometr"),
    @JsonSubTypes.Type(value = Button.class, name = "Button")
    })
public abstract class Sensor {
    private static final String STWORZONO_SENSOR_STRING = "Stworzono Sensor: {}";

    /** Logger Springa */
    @JsonIgnore
    Logger logger;

    /** Id urządzenia w systemie */
    private int id; 
    /** ID Pokoju w, którym jest urządzenie */
    private int room;
    /** adres slave'a*/
    private int slaveID;
    /** ID urzadzenia na płytce drukowanej */
    private int onSlaveID = -1; //domyślnie -1 dla wiadomości, że nie istnieje id tego sensora na slavie
    /** adres urzadzenia */
    private int[] addres;//int ponieważ w arduino byte jest 0-255 a w javie -128-127
    /** ID kolejnego urządzenia w systemie */
    protected static int nextSensorID = 0;
    private String name = "";

    SensorsTypes typ;
    
    protected Sensor() {
        this.id = nextSensorID++;
        this.room = -1;
        this.slaveID = -1;
        this.addres = null;
        this.typ = SensorsTypes.NONE;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info("Stworzono pusty Sensor");
    }
    
    protected Sensor(SensorsTypes type) {
        this.id = nextSensorID++;
        this.room = -1;
        this.slaveID = -1;
        this.addres = null;
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info(STWORZONO_SENSOR_STRING, this);
    }

    protected Sensor(int slaveID, SensorsTypes type){
        this.id = nextSensorID++;
        this.room = -1;
        this.slaveID = slaveID;
        this.addres = null;
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info(STWORZONO_SENSOR_STRING, this);
    }
    
    protected Sensor(int id, int room, int slaveID, byte[] addres,SensorsTypes type){
        this.id = id;
        this.room = room;
        this.slaveID = slaveID;
        setAddres(addres);
        this.typ = type;
        logger = LoggerFactory.getLogger(Sensor.class);
        logger.info(STWORZONO_SENSOR_STRING, this);
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

    @Deprecated
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAddres(byte[] addres) {
        if (addres == null ||addres.length == 0 ) {
            this.addres = new int[8];
            
        }
        else{
            this.addres = new int[8];

            for (int i = 0; i < 8; i++) {
                this.addres[i] = addres[i] & 0xFF;
            }
        }

    }
    
    public void setAddres(int[] addres) {
        if (addres == null || addres.length == 0) {
            this.addres = new int[8];
        }
        else{
            this.addres = Arrays.copyOf(addres, addres.length);
        }
    }

    public SensorsTypes getTyp() {
        return this.typ;
    }

    public void setTyp(SensorsTypes typ) {
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


    @Override
    public String toString() {
        return "{" +
            ", id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", room='" + getRoom() + "'" +
            ", slaveID='" + getSlaveID() + "'" +
            ", onSlaveID='" + getOnSlaveID() + "'" +
            ", addres='" + getAddres() + "'" +
            ", typ='" + getTyp() + "'" +
            "}";
    }

    
    
}

   
