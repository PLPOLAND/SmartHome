package smarthome.model.hardware;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slf4j.LoggerFactory;


public class Blind extends Device{
    DeviceState stan;
    Switch swtUp;
    Switch swtDown;

    Blind(){
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
        if(this.stan != stan){
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(true);
                    swtUp.setStan(false);
                    this.stan = DeviceState.DOWN;
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(false);
                    swtUp.setStan(true);
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
            swtDown.setStan(false);
            swtUp.setStan(true);
            this.stan = DeviceState.UP;
        }else if (this.stan == DeviceState.UP){
            logger.debug("Zmieniam stan na: DOWN");
            swtDown.setStan(true);
            swtUp.setStan(false);
            this.stan = DeviceState.DOWN;
        }
        else if(this.stan == DeviceState.NOTKNOW){
            logger.debug("Jest stan NOTKNOW więc nic nie robię");
        }
    }

    @Deprecated
    public void changeState(boolean stan){
        logger.debug("Zmieniam się na stan: " + (stan?"UP":"DOWN"));
        DeviceState stan2;
        if (stan) {
            stan2 = DeviceState.UP;
        } else {
            stan2 = DeviceState.DOWN;
        }
        this.changeState(stan2);
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

}
