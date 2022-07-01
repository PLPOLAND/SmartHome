package smarthome.model.hardware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ButtonFunction {
    static smarthome.system.System system;

    public enum State {
        NONE,
        UP,
        DOWN,
        STOP
    }
    public enum Type{
        CLICKED,
        HOLDING,
        HOLDED
    }

    Device deviceToControl;
    State state = State.NONE;//0 lub U/D/S
    Type type = Type.CLICKED;
    int clicks = 0;
    
    @JsonBackReference
    Button button;

    public ButtonFunction(smarthome.system.System system) {
        button = null;
        deviceToControl = null;
        state = State.NONE;// 0 lub U/D/S
        clicks = 0;

        ButtonFunction.system = system;
    }
    public ButtonFunction() {
        button = null;
        deviceToControl = null;
        state = State.NONE;// 0 lub U/D/S
        clicks = 0;
    }

    public ButtonFunction(Button button, Device deviceToControl, State state, int clicks, smarthome.system.System system) {
        this.button = button;
        this.deviceToControl = deviceToControl;
        setState(state);
        this.clicks = clicks;
        ButtonFunction.system = system;
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

    public void setType( Type type) {
        this.type = type;
    }

    public Type getType() {
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

    public void fromCommand(int slaveAdress, byte[] command) {
        if (command == null) {
            System.out.println("Command is null");
        }
        if (system == null) {
            System.out.println("System is null");
        }
        button = (Button) ButtonFunction.system.getSensorByOnSlaveID(slaveAdress, command[1]);
        clicks = command[2];
        switch (command[3]) {
            case 'P':
                type = Type.HOLDED;
                break;
            case 'C':
                type = Type.CLICKED;
                break;
            case 'H':
                type = Type.HOLDING;
                break;
            default:
                break;
        }

    }

    public boolean isSimilar(ButtonFunction fun){
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
            st.append("button_id: " + button.getName() + ",");
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
