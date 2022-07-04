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

    
    @Override
    public DeviceState getState() {
        return this.swt.getStan();
    }

    
    @Deprecated
    public void setStan(DeviceState stan) {
        this.swt.setStan(stan);
    }
    
    public void setState(DeviceState stan) {
        this.swt.setStan(stan);
    }

    @Override
    public void changeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla światła. Podany stan = " + state);
        }

        this.swt.setStan(state);
    }

    @Override
    public void changeState() {
        this.swt.setStan(this.swt.getStan().equals(DeviceState.ON) ? DeviceState.OFF : DeviceState.ON);
    }

    @Override
    public void changeToOppositeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla światła. Podany stan = " + state);
        }
        
        if (this.getState() == state) {
            this.changeState();
        }
        else if (this.getState() == DeviceState.NOTKNOW) {
            if (state == DeviceState.ON) {
                this.changeState(DeviceState.OFF);
            }
            else {
                this.changeState(DeviceState.ON);
            }
        }
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
