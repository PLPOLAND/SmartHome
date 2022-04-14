package smarthome.model.hardware;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ButtonFunction {

    public enum State {
        NONE,
        UP,
        DOWN
    }

    Device deviceToControl;
    State state = State.NONE;//0 lub U/D
    int clicks = 0;
    
    @JsonBackReference
    Button button;

    public ButtonFunction() {
        button = null;
        deviceToControl = null;
        state = State.NONE;// 0 lub U/D
        clicks = 0;
    }

    public ButtonFunction(Button button, Device deviceToControl, State state, int clicks) {
        this.button = button;
        this.deviceToControl = deviceToControl;
        setState(state);
        this.clicks = clicks;
    }

    public Button getButton() {
        return this.button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public Device getDevice() {
        return this.deviceToControl;
    }

    public void setDevice(Device deviceToControl) {
        this.deviceToControl = deviceToControl;
    }

    public State getState() {
        return this.state;
    }
    
    @JsonIgnore
    public byte getStateByByte() {
        switch(getState()){
            case NONE:
                return 0;
            case UP: 
                return 'U';
            case DOWN:
                return 'D';
            default:
                return 0;
        }
    }

    public void setState(State state) {
            this.state = state;
    }

    public int getClicks() {
        return this.clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }
    @JsonIgnore
    public int getDeviceOnSlaveID(){
        return deviceToControl.getOnSlaveID();
    }
    
    @JsonIgnore
    public int getButtonOnSlaveID(){
        return button.getOnSlaveID();
    }
    /**
     * 
     * @return przekonwertowaną funkcję do postaci tablicy 4 byte-owej 
     */
    public byte[] toCommand(){
        byte[] command = new byte[4];

        command[0] = (byte)getButtonOnSlaveID();
        command[1] = (byte)getDeviceOnSlaveID();
        command[2] = getStateByByte();
        command[3] = (byte)getClicks();
        return command;
    }


    @Override
    public String toString() {
        return "{" +
            " button_id='" + button.getId() + "'" +
            ", deviceToControl='" + deviceToControl.toString() + "'" +
            ", state='" + getState() + "'" +
            ", clicks='" + getClicks() + "'" +
            "}";
    }
    

}
