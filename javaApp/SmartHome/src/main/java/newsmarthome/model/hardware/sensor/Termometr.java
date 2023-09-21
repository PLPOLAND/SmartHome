package newsmarthome.model.hardware.sensor;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Termometr
 * Dziedziczy po Sensor.
 * @see Sensor
 * @author Marek Pałdyna 
 */
@Component
@Scope("prototype")

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = Higrometr.class, name = "Higrometr"),
    })
public class Termometr extends Sensor{

    /**Aktualna Temperatura */
    Float temperatura;
    /**Maxymalna Temperatura */
    Float maxTemperatura;
    /**Minimalna Temperaturna */
    Float minTemperatura;


    public Termometr(){
        super(SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.maxTemperatura = Float.MIN_VALUE;
        this.minTemperatura = Float.MAX_VALUE;
        logger = LoggerFactory.getLogger(Termometr.class);
    }

    public Termometr(int slaveID){
        super(slaveID, SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.maxTemperatura = Float.MIN_VALUE;
        this.minTemperatura = Float.MAX_VALUE;
        logger = LoggerFactory.getLogger(Termometr.class);
    }
    /**
     * Konstruktor
     * @param id - ID urządzenia w systemie
     * @param room - ID pokoju w którym jest urządzenie
     * @param idPlytki - ID płytki w systemie slave'a
     * @param temperatura - Aktualna temperatura
     * @param max - max temperatura 
     * @param min - min temperatura 
     */
    public Termometr(int id, int room, int idPlytki, int[] addres, Float temperatura, Float max, Float min) {
        super(id ,room, idPlytki, addres, SensorsTypes.THERMOMETR);
        this.temperatura = temperatura;
        this.maxTemperatura = max;
        this.minTemperatura = min;
        logger = LoggerFactory.getLogger(Termometr.class);
    }
    

    public Float getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(Float temperatura) {
        this.temperatura = temperatura;
        if (this.maxTemperatura < temperatura) {
            setMinTemperatura(temperatura); // ustaw minimalną temperaturę 
        }
        if (this.minTemperatura > temperatura) {
            setMaxTemperatura(temperatura); // ustaw maxymalną temperaturę
        }
    }
    public Float getMaxTemperatura() {
        return this.maxTemperatura;
    }
    public void clearMax(){
        this.maxTemperatura=Float.MIN_VALUE;
    }

    public Float getMinTemperatura() {
        return this.minTemperatura;
    }

    public void clearMin() {
        this.minTemperatura = Float.MAX_VALUE;
    }

    public void setMaxTemperatura(Float max) {
        this.maxTemperatura = max;
    }

    public void setMinTemperatura(Float min) {
        this.minTemperatura = min;
    }

    
    /**
     *  Aktualizuje temperaturę, pobierając ją z slave'a
     */
    public void update(){
        this.setTemperatura(slaveSender.checkTemperature(this));
    }

    @Override
    public String toString() {
        return "{" +
            " temperatura='" + getTemperatura() + "'" +
            ", maxTemperatura='" + getMaxTemperatura() + "'" +
            ", minTemperatura='" + getMinTemperatura() + "'" +
            ", super = '"+super.toString()+"'}";
    }

}