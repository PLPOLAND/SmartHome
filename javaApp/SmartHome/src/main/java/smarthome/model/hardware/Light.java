package smarthome.model.hardware;

import org.slf4j.LoggerFactory;

public class Light extends Device{
    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    public Light(){
        super(DeviceTypes.LIGHT);
        swt = new Switch();
        logger = LoggerFactory.getLogger(this.getClass());
    }
    public Light(int pin){
        super(DeviceTypes.LIGHT);
        this.swt = new Switch(false,pin);
        logger = LoggerFactory.getLogger(this.getClass());
    }


    public Light(boolean stan, int pin) {
        super(DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan,pin);
    }

    public Light(boolean stan, int pin, int slaveID) {
        super(slaveID, DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan, pin);
    }

    public Light(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(false,pin);
    }

    public boolean isStan() {
        return this.swt.isStan();
    }

    public boolean getStan() {
        return this.swt.getStan();
    }

    public void setStan(boolean stan) {
        this.swt.setStan(stan);
    }

    public void setPin(int pin){
        swt.setPin(pin);
    }
    public int getPin(){
        return swt.getPin();
    }


    public Switch getSwt() {
        return this.swt;
    }
    

    @Override
    public String toString() {
        return "{" +
            " swt=" + swt.toString() + "" +
            " super = "+ super.toString() +
            "}";
    }
    
    
}
