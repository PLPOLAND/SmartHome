package newsmarthome.model.hardware.device;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import newsmarthome.exception.HardwareException;

@Component
@Scope("prototype")
public class Light extends Device{



    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    
    public Light(){
        super(DeviceTypes.LIGHT);
        swt = new Switch();
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Light created");
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
    public void configureToSlave() {
       try {
        slaveSender.addUrzadzenie(this);
       } catch (HardwareException e) {
           logger.error("Błąd podczas dodawania urządzenia na Slave-a! -> {}", e.getMessage());
       }
    }

    
    @Override
    public DeviceState getState() {
        return this.swt.getStan();
    }


    /**
     * This function sets the state of a device and sends a command to a slave device using I2C
     * communication protocol.
     * 
     * @param stan stan is an object of the DeviceState class, which represents the state of a device
     * (either ON or OFF). The method sets the state of a device to the specified state
     * and sends a command to a slave device via I2C communication.
     */
    public void setState(DeviceState stan) {
        this.swt.setStan(stan);
        try {
            slaveSender.changeSwitchState(getOnSlaveID(), getSlaveID(), stan);
        } catch (HardwareException e) {
            logger.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
        }

    }

    @Override
    public void changeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla światła. Podany stan = " + state);
        }

        this.setState(state);
    }

    @Override
    public void changeState() {
        this.setState(this.swt.getStan().equals(DeviceState.ON) ? DeviceState.OFF : DeviceState.ON);
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
    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.ON || state == DeviceState.OFF;
    }
    
    
}
