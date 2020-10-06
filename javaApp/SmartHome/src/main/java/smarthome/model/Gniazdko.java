package smarthome.model;

public class Gniazdko extends Device{
    boolean stan;

    public Gniazdko(){
        
    }
    public Gniazdko(boolean stan, int pin) {
        super(DeviceTypes.GNIAZDKO, pin);
        this.stan = stan;
    }

    public Gniazdko(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.GNIAZDKO, pin);
        stan = false;
    }

    public boolean isStan() {
        return this.stan;
    }

    public boolean getStan() {
        return this.stan;
    }

    public void setStan(boolean stan) {
        this.stan = stan;
    }

    @Override
    public String toString() {
        return "{" +
            " stan='" + isStan() + "' " +
            super.toString()+
            "}";
    }
    
    
}