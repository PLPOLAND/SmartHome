package newsmarthome.model.hardware.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.extern.log4j.Log4j2;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.wifi.WiFiMasterToSlaveConverter;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Log4j2
public class Blind extends Device {
    DeviceState stan;
    Switch swtUp;
    Switch swtDown;

    @Autowired
    public Blind() {
        super(DeviceTypes.BLIND, false);
        
        swtDown = new Switch();
        swtUp = new Switch();
    }

    public Blind(MasterToSlaveConverter i2CSender, WiFiMasterToSlaveConverter wifiSender) {
        super(DeviceTypes.BLIND, false);
        swtDown = new Switch();
        swtUp = new Switch();
    }
    public Blind(int pinUp, int pinDown, boolean isWifi) {
        super(DeviceTypes.BLIND, isWifi);
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
    }

    public Blind(DeviceState stan, int boardID, int pinUp, int pinDown, boolean isWifi) {
        super(boardID, DeviceTypes.BLIND, isWifi);
        this.changeState(stan);
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
    }

    public Blind(int id, int room, int boardID, int pinUp, int pinDown, boolean isWifi) {
        super(id, room, boardID, DeviceTypes.BLIND, isWifi);
        stan = DeviceState.NOTKNOW;
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
    }

    @Override
    public void configureToSlave() {
        try {
            setOnSlaveID(i2CSender.addUrzadzenie(this));
            setConfigured();
            sendStateToSlave(this.stan);
        } catch (HardwareException e) {
            log.error("Błąd podczas dodawania urządzenia! -> {}", e.getMessage());
            resetConfigured();
        }

    }

    public void setState(DeviceState stan) {
        this.changeState(stan);
    }

    @Override
    public void changeState(DeviceState stan) {
        changeStateLocal(stan);
        sendStateToSlave(stan);
    }

    /**
     * Wysyła stan urządzenia do slave-a.
     * 
     * @param stan - stan urządzenia do wysłania.
     */
    private void sendStateToSlave(DeviceState stan) {
        try {
            if (isConfigured()) {
                log.debug("Wysyłanie stanu urządzenia na slave-a o id: {}", this.getSlaveID());
                i2CSender.changeBlindState(this, stan);
            } else {
                log.debug("Urządzenie nie jest skonfigurowane na slave-ie!");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
        }
    }

    @Override
    public void changeState() {
        if (this.stan == DeviceState.DOWN) {
            this.changeState(DeviceState.UP);
        } else if (this.stan == DeviceState.UP) {
            this.changeState(DeviceState.DOWN);
        } else if (this.stan == DeviceState.RUN) {
            this.changeState(DeviceState.NOTKNOW);
        } else if (this.stan == DeviceState.NOTKNOW) {
            log.debug("Jest stan NOTKNOW więc nic nie robię");
        }
    }

    /**
     * Zmienia stan urządzenia na podany w parametrze. Stan urządzenia jest
     * zmieniany bez wysyłania do slave-a.
     * 
     * @param state - stan na jaki ma zostać zmienione urządzenie.
     */
    private void changeStateLocal(DeviceState state) {
        if (state != DeviceState.UP && state != DeviceState.DOWN && state != DeviceState.NOTKNOW
                && state != DeviceState.RUN) {
            throw new IllegalArgumentException(
                    "Nieprawidłowy stan dla Rolety. Podany stan = " + state + ". Oczekiwany stan = UP, DOWN lub NOTKNOW");
        }

        if (this.stan != state) {
            switch (state) {
                case DOWN:
                    log.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(DeviceState.ON);
                    swtUp.setStan(DeviceState.OFF);
                    this.stan = DeviceState.DOWN;
                    log.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case UP:
                    log.debug("Zmieniam stan na: UP");
                    swtDown.setStan(DeviceState.OFF);
                    swtUp.setStan(DeviceState.ON);
                    this.stan = DeviceState.UP;
                    log.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case NOTKNOW:// TODO Co w tedy?
                    this.stan = DeviceState.NOTKNOW;
                    log.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case RUN:
                    log.debug("Zmieniam stan na: RUN");
                    this.stan = DeviceState.RUN;
                    log.debug("Zmieniono stan urządzenia {}", this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void changeToOppositeState(DeviceState stan) {
        if (stan == DeviceState.NOTKNOW) {
            throw new IllegalArgumentException(
                    "Nie ma stanu przeciwnego dla 'NOTKNOW'; oczekiwano stanu 'UP' lub 'DOWN' lub 'RUN'");
        } else if (stan != DeviceState.UP && stan != DeviceState.DOWN && stan != DeviceState.RUN) {
            throw new IllegalArgumentException(
                    "Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = 'UP' lub 'DOWN' lub 'RUN'");
        }
        if (this.stan == stan) {
            this.changeState();
        } else if (this.stan == DeviceState.NOTKNOW) {
            if (stan == DeviceState.DOWN) {
                this.changeState(DeviceState.UP);
            } else if (stan == DeviceState.UP) {
                this.changeState(DeviceState.DOWN);
            } else {
                this.changeState(DeviceState.NOTKNOW);
            }

        }
    }

    @JsonIgnore
    public int getPinUp() {
        return swtUp.getPin();
    }

    @JsonIgnore
    public int getPinDown() {
        return swtDown.getPin();
    }

    @JsonIgnore
    public void setPinUp(int pin) {
        swtUp.setPin(pin);
    }

    @JsonIgnore
    public void setPinDown(int pin) {
        swtDown.setPin(pin);
    }

    public Switch getSwitchUp() {
        return swtUp;
    }

    public Switch getSwitchDown() {
        return swtDown;
    }

    public void setSwitchUp(Switch swt) {
        swtUp = swt;
    }

    public void setSwitchDown(Switch swt) {
        swtDown = swt;
    }

    @Override
    public DeviceState getState() {
        if (this.stan == null) {
            this.changeState(DeviceState.NOTKNOW);
        }
        return this.stan;
    }

    @Override
    public void updateDeviceState() throws HardwareException, SoftwareException {
        try {
            if (isConfigured()) {
                int state = i2CSender.checkDeviceState(this.getSlaveID(), this.getOnSlaveID());
                if (state == 'U') {
                    this.changeStateLocal(DeviceState.UP);
                } else if (state == 'D') {
                    this.changeStateLocal(DeviceState.DOWN);
                } else if (state == 'K') {
                    this.changeStateLocal(DeviceState.NOTKNOW);
                } else if (state == 'R') {
                    this.changeStateLocal(DeviceState.RUN);
                } else {
                    log.error("Odebrano nieznany stan urządzenia! Stan: {}. DeviceID: {}", state, this.getId());
                    throw new SoftwareException(
                            "Odebrano nieznany stan urządzenia! Stan: " + state + ". DeviceID: " + this.getId(), "U, D, K",
                            String.valueOf(state));
                }
            } else {
                log.debug("Urządzenie nie jest skonfigurowane na slave-ie!");
            }
        } catch (HardwareException e) {
            log.error("Błąd podczas pobierania stanu urządzenia (id:{}; slave:{})! -> {}", this.getId(),
                    this.getSlaveID(), e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            if (e.getResponse()[0] == 'E') {
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        return "{" +
                " stan='" + getState() + "'" +
                ", swtUp='" + swtUp.toString() + "'" +
                ", swtDown='" + swtDown.toString() + "'" +
                ", super ='' " + super.toString() + "'" +
                "}";
    }

    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.UP || state == DeviceState.DOWN || state == DeviceState.NOTKNOW;
    }

}
