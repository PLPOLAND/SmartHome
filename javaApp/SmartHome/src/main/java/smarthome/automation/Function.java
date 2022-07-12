package smarthome.automation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import smarthome.exception.HardwareException;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

/**
 * Function
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AutomationFunction.class, name = "AutomationFunction"),
        @JsonSubTypes.Type(value = ButtonFunction.class, name = "ButtonFunction"),
        @JsonSubTypes.Type(value = UserFunction.class, name = "UserFunction") })
public abstract class Function {



    private int id;
    private String name;
    protected ArrayList<FunctionAction> actions;
    boolean reversState;

    protected Function() {
        id = -1;
        name = "";
        actions = new ArrayList<>();
        reversState = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReversState() {
        return reversState;
    }

    public void setReversState(boolean reversState) {
        this.reversState = reversState;
    }

    public void addAction(FunctionAction action) {
        actions.add(action);
    }
    public void addAction(Device device, DeviceState activeDeviceState, boolean allowReverse) {
        addAction(new FunctionAction(device, activeDeviceState, allowReverse));
    }
    public List<FunctionAction> getActions(){
        return actions;
    }

    public void removeAction(FunctionAction action) {
        actions.remove(action);
    }
    
    public void removeAction(Device device, DeviceState activeDeviceState) {
        for (FunctionAction action : actions) {
            if (action.getDevice().equals(device) && action.getActiveDeviceState().equals(activeDeviceState)) {
                actions.remove(action);
                break;
            }
        }
    }

    public boolean isActive() {
        boolean active = true;
        for (FunctionAction action : actions) {
            if (!action.isActive()) {
                active = false;
                break;
            }
        }
        return active;
    }


    /**
     * Runs the function
     * 
     * @see Function#activate()
     * @see Function#deactivate()
     */
    public abstract void run() throws HardwareException;

    /**
     * Ustawia urządzenia w stan zdefiniowany w akcji jako aktywny
     * 
     * @throws HardwareException
     * 
     * 
     * @see Function#run()
     * @see Function#deactivate()
     */
    public abstract void activate() throws HardwareException;

    /**
     * Ustawia urządzenia w stan przeciwny niż zdefiniowany w akcji jako aktywny
     * 
     * @throws HardwareException
     * 
     * 
     * @see Function#activate()
     * @see Function#run()
     */
    public abstract void deactivate() throws HardwareException;
    

}