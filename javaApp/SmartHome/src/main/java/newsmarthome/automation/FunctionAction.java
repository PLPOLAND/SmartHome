package newsmarthome.automation;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SystemPropertyUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import newsmarthome.exception.HardwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.Blind;



@Component
public class FunctionAction {

    private static MasterToSlaveConverter slave;

    private Device device;
    private DeviceState activeDeviceState;
    boolean allowReverse;//true - można odwrócić stan urządzenia podczas wykonywania akcji

    public FunctionAction() {
        device = null;
        activeDeviceState = DeviceState.NOTKNOW;
        allowReverse = false;
    }
    public FunctionAction(Device device, DeviceState activeDeviceState, boolean allowReverse){
        this.device = device;
        this.activeDeviceState = activeDeviceState;
        this.allowReverse = allowReverse;
    }
    public FunctionAction(int device, DeviceState activeDeviceState, boolean allowReverse){
        // this.device = SmartHomeApp.getApp().getBean(System.class).getDeviceByID(device);//TODO zrobić to lepiej
        this.activeDeviceState = activeDeviceState;
        this.allowReverse = allowReverse;
    }

    public Device getDevice() {
        return device;
    }

    public static void setSlave(@Autowired MasterToSlaveConverter slave) {
        FunctionAction.slave = slave;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public DeviceState getActiveDeviceState() {
        return activeDeviceState;
    }

    public void setActiveDeviceState(DeviceState activeDeviceState) {
        this.activeDeviceState = activeDeviceState;
    }
    
    public boolean isAllowReverse() {
        return allowReverse;
    }

    public void setAllowReverse(boolean allowReverse) {
        this.allowReverse = allowReverse;
    }

    public boolean getAllowReverse() {
        return allowReverse;
    }
    @JsonIgnore
    public boolean isActive() {
        return device.getState() == activeDeviceState;
    }

    /**
     * Zmienia stan urządzenia do stanu zapisanego w activeDeviceState
     * @throws HardwareException
     * @throws IllegalArgumentException kiedy typ urządzenia nie jest jeszcze zaimplementowany
     */
    public void activate() throws HardwareException, IllegalArgumentException{
        device.changeState(activeDeviceState);
    }

    /**
     * Zmienia stan urządzenia do stanu przeciwnego niż zapisany w activeDeviceState
     * 
     * @throws HardwareException
     * @throws IllegalArgumentException kiedy typ urządzenia nie jest jeszcze zaimplementowany
     */
    public void deactivate() throws HardwareException, IllegalArgumentException{
        device.changeToOppositeState(activeDeviceState);
    }
    /**
     * Zmienia stan urządzenia. Jeśli stan urządzenia jest taki sam jak w stanie "aktywnym" i w akcji zezwolono na odwrócenie stanu, to zmienia stan urządzenia na przeciwny. 
     *  W przeciwnym wypadku zmienia stan tylko wtedy kiedy aktualny stan nie jest taki sam jak w stanie "aktywnym".
     * @throws HardwareException
     * @throws IllegalArgumentException kiedy typ urządzenia nie jest jeszcze zaimplementowany
     */
    public void run() throws HardwareException, IllegalArgumentException{
        if(device != null){
            if (device.getState() == activeDeviceState){
                if (allowReverse) {
                    device.changeState();
                }
            }
            else{
                device.changeState(activeDeviceState);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FunctionAction && ((FunctionAction) obj).device.getId() == device.getId();
    }

    public static FunctionAction valueOf(String action) {
        action = action.replace("\"", "");
        action = action.replace("{", "");
        action = action.replace("}", "");
        action = action.replace("[", "");
        action = action.replace("]", "");
        String[] actionParts = action.split(",");

        int deviceID = -1;
        DeviceState state = null;
        boolean allowReverse = false;

        for (String string : actionParts) {
            String[] str = string.split(":");
            switch (str[0]) {
                case "device":
                    deviceID = Integer.parseInt(str[1]);
                    break;
                case "activeDeviceState":
                    state = DeviceState.valueOf(str[1]);
                    break;
                case "allowReverse":
                    allowReverse = Boolean.parseBoolean(str[1]);
                    break;
                default:
                    break;
            }
        }
        
        java.lang.System.out.println(Arrays.toString(actionParts));
        if (deviceID == -1 || state == null){
            return null;
        }
        return new FunctionAction(deviceID, state, allowReverse);
    }
}