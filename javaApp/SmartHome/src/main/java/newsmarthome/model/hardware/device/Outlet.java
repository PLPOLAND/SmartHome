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
public class Outlet extends Device {
    
    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    public Outlet() {
        super(DeviceTypes.GNIAZDKO, false);
        swt = new Switch();
    }

    public Outlet(int pin) {
        super(DeviceTypes.GNIAZDKO, false);
        this.swt = new Switch(DeviceState.OFF, pin);
    }

    public Outlet(DeviceState stan, int pin, int slaveID, boolean isWifi) {
        super(slaveID, DeviceTypes.GNIAZDKO, isWifi);
        this.swt = new Switch(stan, pin);
    }

    public Outlet(int id, int room, int roomID, int pin, boolean isWifi) {
        super(id, room, roomID, DeviceTypes.GNIAZDKO, isWifi);
        this.swt = new Switch(DeviceState.OFF, pin);
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

    public void setState(DeviceState stan) {
        setStateLocal(stan);
        sendStateToSlave(stan);
    }

    private void sendStateToSlave(DeviceState stan) {
        try {
            if (isConfigured()) {
                sender.changeSwitchState(getOnSlaveID(), getSlaveID(), stan);
                log.debug("Zmieniono stan urządzenia {}", this);
            } else {
                log.warn("Urządzenie nie jest skonfigurowane na slave'u!");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
        }
    }

    private void setStateLocal(DeviceState stan) {
        this.swt.setStan(stan);
    }

    @Override
    public void changeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla gniazdka. Podany stan = " + state);
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
            throw new IllegalArgumentException("Nie prawidłowy stan dla gniazdka. Podany stan = " + state);
        }
        
        if (this.getState() == state) {
            this.changeState();
        } else if (this.getState() == DeviceState.NOTKNOW) {
            if (state == DeviceState.ON) {
                this.changeState(DeviceState.OFF);
            } else {
                this.changeState(DeviceState.ON);
            }
        }
    }

    @Override
    public void updateDeviceState() throws SoftwareException, HardwareException {
        try {
            if (isConfigured()) {
                int state = sender.checkDeviceState(getSlaveID(), getOnSlaveID());
                if (state == 1) {
                    this.setStateLocal(DeviceState.ON);
                } else if (state == 0) {
                    this.setStateLocal(DeviceState.OFF);
                } else {
                    log.error("Odebrano nieznany stan urządzenia! -> {}", state);
                    throw new SoftwareException("Odebrano nieznany stan urządzenia! Stan: " + state + ". DeviceID: " + this.getId(), "0,1", String.valueOf(state));
                }
            } else {
                log.debug("Urządzenie nie jest skonfigurowane na slave'u, nie wysyła komend na slave'a.");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas pobierania stanu urządzenia! -> {}", e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            if (e.getResponse()[0] == 'E') {
                throw e;
            }
        }
    }

    public void setPin(int pin) {
        swt.setPin(pin);
    }

    public int getPin() {
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
            " super = " + super.toString() +
            "}";
    }

    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.ON || state == DeviceState.OFF;
    }
}
