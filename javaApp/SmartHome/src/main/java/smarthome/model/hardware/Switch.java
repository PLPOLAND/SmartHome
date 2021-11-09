package smarthome.model.hardware;
/**
 * Przekaźnik
 * 
 */
public class Switch{
    /** ON/OFF */
    private boolean stan;
    /** Numer pinu do sterowania przekaznikiem na Slavie*/ 
    private int pin;
    public Switch(){
        this.pin = -1;
    }
    public Switch(boolean stan, int pin) {
        this.pin = pin;
        this.stan = stan;
    }

    public boolean isStan() {
        return this.stan;
    }

    public boolean getStan() {
        return this.stan;
    }

    public void setStan(boolean stan) {
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
            " stan='" + isStan() + "' " +
            super.toString()+
            "}";
    }
    
    
}