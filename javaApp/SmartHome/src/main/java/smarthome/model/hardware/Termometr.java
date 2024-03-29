package smarthome.model.hardware;

import org.slf4j.LoggerFactory;

/**
 * Termometr
 * Dziedziczy po Sensor.
 * @see Sensor
 * @author Marek Pałdyna 
 */
public class Termometr extends Sensor{

    /**Aktualna Temperatura */
    Float temperatura;
    /**Maxymalna Temperatura */
    Float max;
    /**Minimalna Temperaturna */
    Float min;

    public Termometr(){
        super(SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.max = Float.MIN_VALUE;
        this.min = Float.MAX_VALUE;
        logger = LoggerFactory.getLogger(Termometr.class);
    }

    public Termometr(int slaveID){
        super(slaveID, SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.max = Float.MIN_VALUE;
        this.min = Float.MAX_VALUE;
        logger = LoggerFactory.getLogger(Termometr.class);
    }
    /**
     * Konstruktor
     * @param ID - ID urządzenia w systemie
     * @param room - ID pokoju w którym jest urządzenie
     * @param idPlytki - ID płytki w systemie slave'a
     * @param temperatura - Aktualna temperatura
     * @param max - max temperatura 
     * @param min - min temperatura 
     */
    public Termometr(int ID, int room, int idPlytki, int[] addres, Float temperatura, Float max, Float min) {
        super(ID ,room, idPlytki, addres, SensorsTypes.THERMOMETR);
        this.temperatura = temperatura;
        this.max = max;
        this.min = min;
        logger = LoggerFactory.getLogger(Termometr.class);
    }
    

    public Float getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(Float temperatura) {
        this.temperatura = temperatura;
        if (this.max < temperatura) {
            max = temperatura; // ustaw minimalną temperaturę
        }
        if (this.min > temperatura) {
            min = temperatura; // ustaw maxymalną temperaturę
        }
    }

    public Float getMax() {
        return this.max;
    }
    public void clearMax(){
        this.max=Float.MIN_VALUE;
    }

    public Float getMin() {
        return this.min;
    }

    public void clearMin() {
        this.min = Float.MAX_VALUE;
    }
    
    public void update(){
        
    }

    @Override
    public String toString() {
        return "{" +
            " temperatura='" + getTemperatura() + "'" +
            ", max='" + getMax() + "'" +
            ", min='" + getMin() + "'" +
            ", super = '"+super.toString()+"'}";
    }

}