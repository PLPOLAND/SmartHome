package newsmarthome.model.hardware.device;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.extern.log4j.Log4j2;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.wifi.WiFiMasterToSlaveConverter;

@Component
@Scope("prototype")
@Log4j2
public class Light extends Device{



    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    
    public Light(){
        super(DeviceTypes.LIGHT, false);
        swt = new Switch();
    }
    public Light(int pin){
        super(DeviceTypes.LIGHT, false);
        this.swt = new Switch(DeviceState.OFF,pin);
    }

    public Light(DeviceState stan, int pin, int slaveID, boolean isWifi ) {
        super(slaveID, DeviceTypes.LIGHT, isWifi);
        this.swt = new Switch(stan, pin);
    }

    public Light(int id, int room, int roomID, int pin, boolean isWifi ){
        super(id, room, roomID, DeviceTypes.LIGHT, isWifi);
        this.swt = new Switch(DeviceState.OFF,pin);
    }    

    @Override
    public void configureToSlave() {
       try {
            setOnSlaveID(sender.addUrzadzenie(this));
            setConfigured();
            sendStateToSlave(this.getState());
       } catch (HardwareException e) {
           log.error("Błąd podczas dodawania urządzenia na Slave-a! -> {}", e.getMessage());
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
                sender.changeSwitchState(getOnSlaveID(), getSlaveID(), stan);
                log.debug("Zmieniono stan urządzenia {}" , this);
            }
            else{
                log.warn("Urządzenie nie jest skonfigurowane na slave-u!");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
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
                int state = sender.checkDeviceState(getSlaveID(), getOnSlaveID());
                if (state == 1) {
                    this.setStateLocal(DeviceState.ON);
                }
                else if (state == 0) {
                    this.setStateLocal(DeviceState.OFF);
                }
                else {
                    log.error("Odebrano nieznany stan urządzenia! -> {}", state);
                    throw new SoftwareException("Odebrano nieznany stan urządzenia! Stan: " + state + ". DeviceID: " + this.getId(), "0,1", String.valueOf(state));
                
                }
            }
            else{
                log.debug("Urządzenie nie jest skonfigurowane na slave'u, nie wysyła komend na slave'a.");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas pobierania stanu urządzenia (id:{}; slave:{})! -> {}",this.getId(),this.getSlaveID(), e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
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
