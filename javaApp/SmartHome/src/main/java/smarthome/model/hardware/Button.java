package smarthome.model.hardware;

import org.slf4j.LoggerFactory;

public class Button extends Sensor{
    

    /** Numer pinu do sterowania przekaznikiem na Slavie */
    private int pin;

    //TODO dodać przechowywanie funkcji.
    public Button(){
        super(SensorsTypes.BUTTON);
        logger = LoggerFactory.getLogger(Button.class);
    }

    public Button(int slaveID){
        super(slaveID, SensorsTypes.BUTTON);
        logger = LoggerFactory.getLogger(Button.class);
        this.pin = -1;
    }
    public Button(int slaveID, int pin){
        super(slaveID, SensorsTypes.BUTTON);
        logger = LoggerFactory.getLogger(Button.class);
        this.pin = pin;
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
            " pin='" + getPin() + "'," +
            "super= " + super.toString()+
            "}";
    }
    

}
