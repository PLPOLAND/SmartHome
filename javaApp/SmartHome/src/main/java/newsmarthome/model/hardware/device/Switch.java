package newsmarthome.model.hardware.device;

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
        this.stan = DeviceState.OFF;
    }
    public Switch(DeviceState stan, int pin) {
        this.pin = pin;
        if (stan != DeviceState.ON && stan != DeviceState.OFF) {
            throw new IllegalArgumentException("Stan może być tylko ON lub OFF");
        }
        this.stan = stan;
    }

    public DeviceState getStan() {
        return this.stan;
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
            " stan='" + getStan() + "'" +
            ", pin='" + getPin() + "'" +
            "}";
    }
    
    
    
}