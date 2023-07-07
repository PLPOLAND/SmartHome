package newsmarthome.model.hardware.device;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;

@Component
@Scope("prototype")
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
    public void configureToSlave() {
       try {
            setOnSlaveID(slaveSender.addUrzadzenie(this));
            setConfigured();
            sendStateToSlave(this.getState());
       } catch (HardwareException e) {
           logger.error("Błąd podczas dodawania urządzenia na Slave-a! -> {}", e.getMessage());
           resetConfigured();
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
        setStateLocal(stan);
        sendStateToSlave(stan);
    }

    /**
     * Wyślij stan urządzenia do slave-a
     * @param stan - stan urządzenia do wysłania
     */
    private void sendStateToSlave(DeviceState stan) {
        try {
            if (isConfigured()) {
                slaveSender.changeSwitchState(getOnSlaveID(), getSlaveID(), stan);
                logger.debug("Zmieniono stan urządzenia {}" , this);
            }
            else{
                logger.warn("Urządzenie nie jest skonfigurowane na slave-u!");
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
        }
    }
    /**
     * This function sets the state of a device and doesn't send a command to a slave device using I2C
     * @param stan 
     */
    private void setStateLocal(DeviceState stan) {
        this.swt.setStan(stan);
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

    @Override
    public void updateDeviceState() throws HardwareException, SoftwareException{
        try {
            if (isConfigured()) {
                int state = slaveSender.checkDeviceState(getSlaveID(), getOnSlaveID());
                if (state == 1) {
                    this.setStateLocal(DeviceState.ON);
                }
                else if (state == 0) {
                    this.setStateLocal(DeviceState.OFF);
                }
                else {
                    logger.error("Odebrano nieznany stan urządzenia! -> {}", state);
                    throw new SoftwareException("Odebrano nieznany stan urządzenia! Stan: " + state + ". DeviceID: " + this.getId(), "0,1", String.valueOf(state));
                
                }
            }
            else{
                logger.debug("Urządzenie nie jest skonfigurowane na slave'u, nie wysyła komend na slave'a.");
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas pobierania stanu urządzenia (id:{}; slave:{})! -> {}",this.getId(),this.getSlaveID(), e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            if (e.getResponse()[0] == 'E') {
                throw e;
            }
        }
    }

    public void setPin(int pin){
        swt.setPin(pin);
    }
    public int getPin(){
        return swt.getPin();
    }

    @JsonIgnore
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
    @JsonIgnore
    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.ON || state == DeviceState.OFF;
    }
    
    
}
