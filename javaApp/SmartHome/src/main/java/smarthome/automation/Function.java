package smarthome.automation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import smarthome.SmartHomeApp;
import smarthome.database.AutomationDAO;
import smarthome.exception.HardwareException;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;
import smarthome.model.hardware.DeviceTypes;

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

    public enum FunctionType {
        NOTKNOWN, AUTOMATION, BUTTON, USER;
        
        public static String[] getNames() {
            return Arrays.stream(FunctionType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
        }
    }



    private int id;
    private String name;
    protected ArrayList<FunctionAction> actions;
    boolean reversState;
    FunctionType type;

    protected Function() {
        id = -1;
        name = "";
        actions = new ArrayList<>();
        reversState = false;
        type = FunctionType.NOTKNOWN;
    }

    protected Function(FunctionType type) {
        id = -1;
        name = "";
        actions = new ArrayList<>();
        reversState = false;
        this.type = type;
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
        SmartHomeApp.getApp().getBean(AutomationDAO.class).save(this);
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
    
    public void clearActions() {
        actions.clear();
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

    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type) {
        this.type = type;
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
    

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", actions='" + getActions() + "'" +
            ", reversState='" + isReversState() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }

    public static String[] getFunctionTypes() {
        return FunctionType.getNames();
    }
    
}