package smarthome.model;

public class Roleta extends Device{
    enum RoletaStan{
        DOWN,
        NOTKNOW,
        UP
    }    

    RoletaStan stan;

    Roleta(){
        super();
    }
    
    public Roleta(boolean stan, int pin) {
        super(DeviceTypes.GNIAZDKO, pin);
        this.stan = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
    }

    public Roleta(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.GNIAZDKO, pin);
        stan = RoletaStan.DOWN;
    }

    public void changeState(RoletaStan stan){
        if(!this.stan.equals(stan)){
            //TODO
        }
    }

    public void changeState(boolean stan){
        RoletaStan stan2 = stan == true ? RoletaStan.UP : RoletaStan.DOWN;
        this.changeState(stan2);
    }

}
