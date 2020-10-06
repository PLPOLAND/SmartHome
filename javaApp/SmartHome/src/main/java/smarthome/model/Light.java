package smarthome.model;

public class Light extends Device{

    boolean stan;

    public Light(){
        super(DeviceTypes.SWIATLO);
        this.stan = false;
    }
    public Light(int pin){
        super(DeviceTypes.SWIATLO, pin);
        this.stan = false;
    }


    public Light(boolean stan, int pin) {
        super(DeviceTypes.SWIATLO, pin);
        this.stan = stan;
    }

    public Light(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.SWIATLO, pin);
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
            " stan='" + isStan() + "'" +
            super.toString()+
            "}";
    }
    
    
}
