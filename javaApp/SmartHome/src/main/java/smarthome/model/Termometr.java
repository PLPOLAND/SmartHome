package smarthome.model;

/**
 * Termometr
 * Dziedziczy po Device.
 * @see Device
 * @author Marek Pałdyna 
 */
public class Termometr extends Device{

    /**Aktualna Temperatura */
    Double temperatura;
    /**Maxymalna Temperatura */
    Double max;
    /**Minimalna Temperaturna */
    Double min;

    public Termometr(){
        super();
        this.temperatura = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
        this.min = Double.MAX_VALUE;
    }

    /**
     * Konstruktor
     * @param ID - ID urządzenia w systemie
     * @param room - ID pokoju w którym jest urządzenie
     * @param roomID - ID urządzenia na płytce drukowanej
     * @param temperatura - Aktualna temperatura
     * @param max - max temperatura 
     * @param min - min temperatura 
     */
    public Termometr(int ID, int room, int roomID, Double temperatura, Double max, Double min) {
        super(ID ,room, roomID);
        this.temperatura = temperatura;
        this.max = max;
        this.min = min;
    }
    

    public Double getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
        if (this.max < temperatura) {
            max = temperatura; // ustaw minimalną temperaturę
        }
        if (this.min > temperatura) {
            min = temperatura; // ustaw maxymalną temperaturę
        }
    }

    public Double getMax() {
        return this.max;
    }
    public void clearMax(){
        this.max=Double.MIN_VALUE;
    }

    public Double getMin() {
        return this.min;
    }

    public void clearMin() {
        this.min = Double.MAX_VALUE;
    }
    

}