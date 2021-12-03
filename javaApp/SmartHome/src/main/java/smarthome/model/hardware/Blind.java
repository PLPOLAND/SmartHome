package smarthome.model.hardware;


public class Blind extends Device{//TODO do przerobienia na używanie 2x przekaźnik
    enum RoletaStan{
        DOWN,
        NOTKNOW,
        UP
    }    

    RoletaStan stan;
    Switch swtUp;
    Switch swtDown;

    Blind(){
        super(DeviceTypes.BLIND);
    }
    
    public Blind(boolean stan, int boardID, int pinUp, int pinDown) {//TODO
        super(boardID, DeviceTypes.BLIND);
        this.stan = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
        swtUp = new Switch(false, pinUp);
        swtDown = new Switch(false, pinDown);
    }

    public Blind(int id, int room, int boardID, int pinUp, int pinDown){//TODO
        super(id, room, boardID, DeviceTypes.BLIND);
        stan = RoletaStan.DOWN;
        swtUp = new Switch(false, pinUp);
        swtDown = new Switch(false, pinDown);
    }

    public void changeState(RoletaStan stan){
        if(!this.stan.equals(stan)){
            switch (stan) {
                case DOWN:
                    swtDown.setStan(true);
                    swtUp.setStan(false);
                    break;
                case UP:
                    swtDown.setStan(false);
                    swtUp.setStan(true);
                    break;
                case NOTKNOW://TODO Co w tedy?
                    break;
                default:
                    break;
            }
        }
    }

    public void changeState(boolean stan){
        RoletaStan stan2 = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
        this.changeState(stan2);
    }

    public int getPinUp(){
        return swtUp.getPin();
    }
    public int getPinDown(){
        return swtDown.getPin();
    }
}
