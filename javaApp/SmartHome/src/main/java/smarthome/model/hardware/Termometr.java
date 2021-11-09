package smarthome.model.hardware;

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
    /**Numer na płytce*/
    byte numberOnBoard;

    public Termometr(){
        super(SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.max = Float.MIN_VALUE;
        this.min = Float.MAX_VALUE;
    }
    public Termometr(int pin){
        super(SensorsTypes.THERMOMETR);
        this.temperatura = Float.MAX_VALUE;
        this.max = Float.MIN_VALUE;
        this.min = Float.MAX_VALUE;
    }

    /**
     * Konstruktor
     * @param ID - ID urządzenia w systemie
     * @param room - ID pokoju w którym jest urządzenie
     * @param idPlytki - ID płytki w systemie
     * @param temperatura - Aktualna temperatura
     * @param max - max temperatura 
     * @param min - min temperatura 
     */
    public Termometr(int ID, int room, int idPlytki, Float temperatura, Float max, Float min) {
        super(ID ,room, idPlytki, SensorsTypes.THERMOMETR);
        this.temperatura = temperatura;
        this.max = max;
        this.min = min;
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

    public byte getNumberOnBoard() {
        return this.numberOnBoard;
    }

    public void setNumberOnBoard(byte numberOnBoard) {
        this.numberOnBoard = numberOnBoard;
    }
    

}