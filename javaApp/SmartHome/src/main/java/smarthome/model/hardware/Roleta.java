package smarthome.model.hardware;


public class Roleta extends Device{//TODO do przerobienia na używanie 2x przekaźnik
    enum RoletaStan{
        DOWN,
        NOTKNOW,
        UP
    }    

    RoletaStan stan;
    Switch swtUp;
    Switch swtDown;

    Roleta(){
        super(DeviceTypes.BLIND);
    }
    
    public Roleta(boolean stan, int pinUp, int pinDown) {//TODO
        super(DeviceTypes.BLIND);
        this.stan = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
        swtUp = new Switch(false, pinUp);
        swtDown = new Switch(false, pinDown);
    }

    public Roleta(int id, int room, int roomID, int pinUp, int pinDown){//TODO
        super(id, room, roomID, DeviceTypes.BLIND);
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

}
