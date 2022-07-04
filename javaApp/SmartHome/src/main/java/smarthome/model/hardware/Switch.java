package smarthome.model.hardware;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Przekaźnik
 * 
 */
public class Switch{
    /** ON/OFF */
    private DeviceState stan;
    /** Numer pinu do sterowania przekaznikiem na Slavie*/ 
    private int pin;
    public Switch(){
        this.pin = -1;
    }
    public Switch(DeviceState stan, int pin) {
        this.pin = pin;
        if (stan != DeviceState.ON && stan != DeviceState.OFF) {
            throw new IllegalArgumentException("Stan może być tylko ON lub OFF");
        }
        this.stan = stan;
    }

    @JsonIgnore
    public boolean isStan() {
        return this.stan == DeviceState.ON;
    }

    public DeviceState getStan() {
        return this.stan;
    }

    
    public void setStan(boolean stan) {
        this.stan = stan?DeviceState.ON:DeviceState.OFF;
    }

    public void setStan(DeviceState stan) {
        if (stan!=DeviceState.ON && stan!=DeviceState.OFF) {
            throw new IllegalArgumentException("Stan może być tylko ON lub OFF");
        }
        this.stan = stan;
    }

    public int getPin() {
        return this.pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }



    @Override
    public String toString() {
        return "{" +
            " stan='" + isStan() + "'" +
            ", pin='" + getPin() + "'" +
            "}";
    }
    
    
    
}