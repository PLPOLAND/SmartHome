package smarthome.i2c;

import java.util.Arrays;

public class I2CCommand {
    //id of the command
    private final int id;
    //the command to send to the device
    private final Byte[] command;
    //address of the device to send the command to
    private final int address;
    //current state of processing the command the command
    private State state = State.CREATED;
    private I2CResponse response = null;

    public I2CCommand(int id, Byte[] command, int address) {
        this.id = id;
        this.command = command;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public Byte[] getCommand() {
        return command;
    }

    public int getAddress() {
        return address;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public I2CResponse getResponse() {
        return response;
    }

    public void setResponse(I2CResponse response) {
        this.response = response;
    }


    @Override
    public String toString() {
        return "I2CCommand [id=" + id + ", command=" + Arrays.toString(command) + ", address=" + address + ", state="
                + state + ", response=" + response + "]";
    }

    public String toJSONString(){
        return "{\"id\":"+id+",\"command\":"+Arrays.toString(command)+",\"address\":"+address+"}";
    }

    /**
     * The state of procesing the command
     */
    public enum State{
        CREATED,//command created
        SENT,// command sent to the device
        RECIEVED,//response recieved from the device
        PROCESSED,//response processed
        ERROR;//error occured
    }
}
