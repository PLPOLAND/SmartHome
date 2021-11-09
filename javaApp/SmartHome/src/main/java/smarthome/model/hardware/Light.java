package smarthome.model.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.model.hardware.Device;

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

    @Override
    public String toString() {
        return "{" +
            " stan='" + isStan() + "'" +
            super.toString()+
            "}";
    }
    
    
}
