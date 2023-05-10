package newsmarthome.model.hardware.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Blind extends Device{
    DeviceState stan;
    Switch swtUp;
    Switch swtDown;

    public Blind(){
        super(DeviceTypes.BLIND);
        logger = LoggerFactory.getLogger(Blind.class);
    }
    
    public Blind(boolean stan, int boardID, int pinUp, int pinDown) {
        super(boardID, DeviceTypes.BLIND);
        this.stan = stan? DeviceState.UP : DeviceState.DOWN;
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
    public void changeState(DeviceState stan){
        if (stan != DeviceState.UP && stan != DeviceState.DOWN && stan != DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = UP, DOWN lub NOTKNOW");
        }

        if(this.stan != stan){
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(DeviceState.ON);
                    swtUp.setStan(DeviceState.OFF);
                    this.stan = DeviceState.DOWN;
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(DeviceState.OFF);
                    swtUp.setStan(DeviceState.ON);
                    this.stan = DeviceState.UP;
                    break;
                case NOTKNOW://TODO Co w tedy?
                    this.stan = DeviceState.NOTKNOW;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void changeState(){
        if(this.stan == DeviceState.DOWN){
            logger.debug("Zmieniam stan na: UP");
            swtDown.setStan(DeviceState.OFF);
            swtUp.setStan(DeviceState.ON);
            this.stan = DeviceState.UP;
        }else if (this.stan == DeviceState.UP){
            logger.debug("Zmieniam stan na: DOWN");
            swtDown.setStan(DeviceState.ON);
            swtUp.setStan(DeviceState.OFF);
            this.stan = DeviceState.DOWN;
        }
        else if(this.stan == DeviceState.NOTKNOW){
            logger.debug("Jest stan NOTKNOW więc nic nie robię");
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
