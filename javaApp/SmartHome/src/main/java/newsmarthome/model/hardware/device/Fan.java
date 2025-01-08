package newsmarthome.model.hardware.device;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.wifi.WiFiMasterToSlaveConverter;

@Component
@Scope("prototype")
public class Fan extends Device {
    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    public Fan() {
        super(DeviceTypes.WENTYLATOR, false);
        swt = new Switch();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Fan(int pin) {
        super(DeviceTypes.WENTYLATOR, false);
        this.swt = new Switch(DeviceState.OFF, pin);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Fan(DeviceState stan, int pin, int slaveID, boolean isWifi) {
        super(slaveID, DeviceTypes.WENTYLATOR, isWifi);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan, pin);
    }

    @Override
    public void configureToSlave() {
        try {
            setOnSlaveID(sender.addUrzadzenie(this));
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

    public void setState(DeviceState stan) {
        setStateLocal(stan);
        sendStateToSlave(stan);
    }

    private void sendStateToSlave(DeviceState stan) {
        try {
            if (isConfigured()) {
                sender.changeSwitchState(getOnSlaveID(), getSlaveID(), stan);
                logger.debug("Zmieniono stan urządzenia {}", this);
            } else {
                logger.warn("Urządzenie nie jest skonfigurowane na slave'u!");
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
        }
    }

    private void setStateLocal(DeviceState stan) {
        this.swt.setStan(stan);
    }

    @Override
    public void changeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla wentylatora. Podany stan = " + state);
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
            throw new IllegalArgumentException("Nie prawidłowy stan dla wentylatora. Podany stan = " + state);
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
                    logger.error("Odebrano nieznany stan urządzenia! -> {}", state);
                    throw new SoftwareException("Odebrano nieznany stan urządzenia! Stan: " + state + ". DeviceID: " + this.getId(), "0,1", String.valueOf(state));
                }
            } else {
                logger.warn("Urządzenie nie jest skonfigurowane na slave-u!");
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas pobierania stanu urządzenia! -> {}", e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
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
