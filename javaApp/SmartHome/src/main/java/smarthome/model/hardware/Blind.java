package smarthome.model.hardware;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slf4j.LoggerFactory;


public class Blind extends Device{
    public enum RoletaStan{
        DOWN,
        NOTKNOW,
        UP
    }    

    RoletaStan stan;
    Switch swtUp;
    Switch swtDown;

    Blind(){
        super(DeviceTypes.BLIND);
        logger = LoggerFactory.getLogger(Blind.class);
    }
    
    public Blind(boolean stan, int boardID, int pinUp, int pinDown) {
        super(boardID, DeviceTypes.BLIND);
        this.stan = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
        swtUp = new Switch(false, pinUp);
        swtDown = new Switch(false, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    public Blind(int id, int room, int boardID, int pinUp, int pinDown){
        super(id, room, boardID, DeviceTypes.BLIND);
        stan = RoletaStan.DOWN;
        swtUp = new Switch(false, pinUp);
        swtDown = new Switch(false, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    public void changeState(RoletaStan stan){
        if(this.stan != stan){
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(true);
                    swtUp.setStan(false);
                    this.stan = RoletaStan.DOWN;
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(false);
                    swtUp.setStan(true);
                    this.stan = RoletaStan.UP;
                    break;
                case NOTKNOW://TODO Co w tedy?
                    this.stan = RoletaStan.NOTKNOW;
                    break;
                default:
                    break;
            }
        }
    }

    public void changeState(boolean stan){
        logger.debug("Zmieniam się na stan: " + (stan?"UP":"DOWN"));
        RoletaStan stan2;
        if (stan) {
            stan2 = RoletaStan.UP;
        } else {
            stan2 = RoletaStan.DOWN;
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



    public RoletaStan getStan(){
        return this.stan;
    }

    @Override
    public String toString() {
        return "{" +
            " stan='" + getStan() + "'" +
            ", swtUp='" + swtUp.toString() + "'" +
            ", swtDown='" + swtDown.toString() + "'" +
            ", super ='' " + super.toString() + "'"+
            "}";
    }

}
