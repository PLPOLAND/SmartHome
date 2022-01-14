package smarthome.model.hardware;

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

    public int getPinUp(){
        return swtUp.getPin();
    }
    public int getPinDown(){
        return swtDown.getPin();
    }

    public RoletaStan getStan(){
        return this.stan;
    }
}
