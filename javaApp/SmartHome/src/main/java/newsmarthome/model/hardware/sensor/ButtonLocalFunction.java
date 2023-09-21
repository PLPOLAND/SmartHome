package newsmarthome.model.hardware.sensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import newsmarthome.model.hardware.device.Device;

@Component
public class ButtonLocalFunction {

    public enum State {
        NONE,
        UP,
        DOWN,
        STOP
    }
    Device deviceToControl;
    State state = State.NONE;//0 lub U/D/S
    ButtonClickType type = ButtonClickType.CLICKED;
    int clicks = 0;
    
    @JsonBackReference
    Button button;

    public ButtonLocalFunction() {
        button = null;
        deviceToControl = null;
        state = State.NONE;// 0 lub U/D/S
        clicks = 0;
    }

    // public ButtonFunction(Button button, Device deviceToControl, State state, int clicks, smarthome.system.System system) {
    //     this.button = button;
    //     this.deviceToControl = deviceToControl;
    //     setState(state);
    //     this.clicks = clicks;
    //     ButtonFunction.system = system;
    // }
    public ButtonLocalFunction(Button button, Device deviceToControl, State state, int clicks) {
        this.button = button;
        this.deviceToControl = deviceToControl;
        setState(state);
        this.clicks = clicks;
        // ButtonFunction.system = system;
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
            case STOP:
                return 'S';
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

    public void setType( ButtonClickType type) {
        this.type = type;
    }

    public ButtonClickType getType() {
        return this.type;
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


    public boolean isSimilar(ButtonLocalFunction fun){
        if(fun.getType() == getType() && fun.getClicks() == getClicks() && fun.getButton().equals(button)){
            return true;
        }
        else
            return false;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("{");
        if (button != null) {
            st.append("button_id: " + button.getNazwa() + ",");
        }
        if (deviceToControl != null) {
            st.append(", deviceToControl='" + deviceToControl.toString() + "'" );
        }
        st.append(", state='" + getState() + "'" +
            ", clicks='" + getClicks() + "'" +
            "}");
        return st.toString();
    }
    

}
