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
        this.swt = new Switch(DeviceState.OFF,pin);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Deprecated
    public Light(boolean stan, int pin, int slaveID) {
        super(slaveID, DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan ? DeviceState.ON: DeviceState.OFF, pin);
    }

    public Light(DeviceState stan, int pin, int slaveID) {
        super(slaveID, DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan, pin);
    }

    public Light(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.LIGHT);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(DeviceState.OFF,pin);
    }

    public boolean isStan() {
        return this.swt.isStan();
    }

    public DeviceState getStan() {
        return this.swt.getStan();
    }
    @Override
    public DeviceState getState() {
        return this.swt.getStan();
    }

    @Deprecated
    public void setStan(boolean stan) {
        this.swt.setStan(stan);
    }

    public void setStan(DeviceState stan) {
        this.swt.setStan(stan);
    }

    @Override
    public void changeState(DeviceState stan) {
        this.swt.setStan(stan);
    }
    @Override
    public void changeState() {
        this.swt.setStan(this.swt.getStan().equals(DeviceState.ON) ? DeviceState.OFF : DeviceState.ON);
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
