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

    /** Wartość zwracana przez DS18B20, gdy czujnik jest odłączony (DEVICE_DISCONNECTED_C) */
    public static final float DISCONNECTED_TEMPERATURE = -127.0f;

    /**Aktualna Temperatura; volatile - czytana też przez publisher MQTT */
    volatile Float temperatura;
    /**Maxymalna Temperatura */
    Float maxTemperatura;
    /**Minimalna Temperaturna */
    Float minTemperatura;


    public Termometr(){
        super(SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.maxTemperatura = -Float.MAX_VALUE;
        this.minTemperatura = Float.MAX_VALUE;
        logger = LoggerFactory.getLogger(Termometr.class);
    }

    public Termometr(int slaveID){
        super(slaveID, SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.maxTemperatura = -Float.MAX_VALUE;
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
        if (temperatura == null) {
            return; // brak odczytu - zostaje ostatnia poprawna wartość
        }
        this.temperatura = temperatura;
        if (this.maxTemperatura == null || this.maxTemperatura < temperatura) {
            setMaxTemperatura(temperatura);
        }
        if (this.minTemperatura == null || this.minTemperatura > temperatura) {
            setMinTemperatura(temperatura);
        }
    }

    /**
     * Zamienia tekst temperatury wysłany przez slave'a (np. "21.19", "-3.50") na liczbę.
     * @param bytes odpowiedź slave'a
     * @param length liczba bajtów odpowiedzi zawierających temperaturę
     * @return temperatura albo null, gdy odpowiedź nie zawiera liczby
     */
    public static Float parseTemperature(byte[] bytes, int length) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length && i < bytes.length; i++) {
            byte b = bytes[i];
            if (b >= '0' && b <= '9' || b == '.' || (b == '-' && text.length() == 0)) {
                text.append((char) b);
            }
        }
        try {
            return Float.parseFloat(text.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    public Float getMaxTemperatura() {
        return this.maxTemperatura;
    }
    public void clearMax(){
        this.maxTemperatura = -Float.MAX_VALUE;
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
        Float odczyt = slaveSender.checkTemperature(this);
        if (odczyt == null) {
            logger.warn("Brak odczytu temperatury z termometru {} - zostaje poprzednia wartość", getId());
            return;
        }
        this.setTemperatura(odczyt);
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