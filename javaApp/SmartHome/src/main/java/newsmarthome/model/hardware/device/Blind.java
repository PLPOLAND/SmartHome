package newsmarthome.model.hardware.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import newsmarthome.exception.HardwareException;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Blind extends Device{
    DeviceState stan;
    Switch swtUp;
    Switch swtDown;

    public Blind(){
        super(DeviceTypes.BLIND);
        logger = LoggerFactory.getLogger(Blind.class);
        swtDown = new Switch();
        swtUp = new Switch();
    }

    public Blind(int pinUp, int pinDown){
        super(DeviceTypes.BLIND);
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }
    
    public Blind(DeviceState stan, int boardID, int pinUp, int pinDown) {
        super(boardID, DeviceTypes.BLIND);
        this.changeState(stan);
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    public Blind(int id, int room, int boardID, int pinUp, int pinDown){
        super(id, room, boardID, DeviceTypes.BLIND);
        stan = DeviceState.NOTKNOW;
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    @Override
    public void configureToSlave(){
        try {
            setOnSlaveID(slaveSender.addUrzadzenie(this));
            setConfigured();
        } catch (HardwareException e) {
            logger.error("Błąd podczas dodawania urządzenia! -> {}", e.getMessage());
            resetConfigured();
        }

    }

    public void setState(DeviceState stan){
        this.changeState(stan);
    }

    @Override
    public void changeState(DeviceState stan){
        if (stan != DeviceState.UP && stan != DeviceState.DOWN && stan != DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = UP, DOWN lub NOTKNOW");
        }

        if (this.stan != stan) {
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(DeviceState.ON);
                    swtUp.setStan(DeviceState.OFF);
                    this.stan = DeviceState.DOWN;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(DeviceState.OFF);
                    swtUp.setStan(DeviceState.ON);
                    this.stan = DeviceState.UP;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case NOTKNOW:// TODO Co w tedy?
                    this.stan = DeviceState.NOTKNOW;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                default:
                    break;
            }
            try {
                if (isConfigured()) {
                    logger.debug("Wysyłanie stanu urządzenia na slave-a o id: {}", this.getSlaveID());
                    slaveSender.changeBlindState(this, stan);
                }
                else{
                    logger.debug("Urządzenie nie jest skonfigurowane na slave-ie!");
                }
            } catch (HardwareException e) {
                logger.error("Błąd podczas zmiany stanu urządzenia! -> {}", e.getMessage());
            }
        }
    }

    @Override
    public void changeState(){
        if(this.stan == DeviceState.DOWN){
            this.changeState(DeviceState.UP);
        }else if (this.stan == DeviceState.UP){
            this.changeState(DeviceState.DOWN);
        }
        else if(this.stan == DeviceState.NOTKNOW){
            logger.debug("Jest stan NOTKNOW więc nic nie robię");
        }
    }

    private void changeStateLocal(DeviceState stan){
        if (stan != DeviceState.UP && stan != DeviceState.DOWN && stan != DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = UP, DOWN lub NOTKNOW");
        }

        if (this.stan != stan) {
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(DeviceState.ON);
                    swtUp.setStan(DeviceState.OFF);
                    this.stan = DeviceState.DOWN;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(DeviceState.OFF);
                    swtUp.setStan(DeviceState.ON);
                    this.stan = DeviceState.UP;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                case NOTKNOW:// TODO Co w tedy?
                    this.stan = DeviceState.NOTKNOW;
                    logger.debug("Zmieniono stan urządzenia {}", this);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void changeToOppositeState( DeviceState stan){
        if (stan == DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nie ma stanu przeciwnego dla 'NOTKNOW'; oczekiwano stanu 'UP' lub 'DOWN'");
        }
        else if (stan!=DeviceState.UP && stan!=DeviceState.DOWN) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = 'UP' lub 'DOWN'");
        }
        if(this.stan == stan){
            this.changeState();
        }
        else if (this.stan == DeviceState.NOTKNOW){
            if (stan == DeviceState.DOWN) {
                this.changeState(DeviceState.UP);
            } else {
                this.changeState(DeviceState.DOWN);
            }
        }
    }

    @JsonIgnore
    public int getPinUp(){
        return swtUp.getPin();
    }
    
    @JsonIgnore
    public int getPinDown(){
        return swtDown.getPin();
    }
    @JsonIgnore
    public void setPinUp(int pin){
        swtUp.setPin(pin);
    }
    
    @JsonIgnore
    public void setPinDown(int pin){
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
    public DeviceState getState(){
        if (this.stan == null) {
            this.changeState(DeviceState.NOTKNOW);
        }
        return this.stan;
    }
    @Override
    public void updateDeviceState(){
        try {
            if (isConfigured()) {
                int state = slaveSender.checkDeviceState(this.getSlaveID(), this.getOnSlaveID());
                if (state == 'U') {
                    this.changeStateLocal(DeviceState.UP);
                }
                else if (state == 'D') {
                    this.changeStateLocal(DeviceState.DOWN);
                }
                else if (state == 'K') {
                    this.changeStateLocal(DeviceState.NOTKNOW);
                }
                else{
                    logger.error("Odebrano nieznany stan urządzenia! Stan: {}. DeviceID: {}", state, this.getId());
                }
            }
            else{
                logger.debug("Urządzenie nie jest skonfigurowane na slave-ie!");
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas pobierania stanu urządzenia! -> {}", e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "{" +
            " stan='" + getState() + "'" +
            ", swtUp='" + swtUp.toString() + "'" +
            ", swtDown='" + swtDown.toString() + "'" +
            ", super ='' " + super.toString() + "'"+
            "}";
    }

    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.UP || state == DeviceState.DOWN || state == DeviceState.NOTKNOW;
    }

}
