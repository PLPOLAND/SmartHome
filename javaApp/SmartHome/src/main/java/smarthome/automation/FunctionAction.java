package smarthome.automation;

import java.util.ArrayList;

import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

public class FunctionAction {
    private Device device;
    private DeviceState activeDeviceState;

    FunctionAction(){
        device = null;
        activeDeviceState = DeviceState.NOTKNOW;
    }
    FunctionAction(Device device, DeviceState activeDeviceState){
        this.device = device;
        this.activeDeviceState = activeDeviceState;
    }

    public Device getDevice() {
        return device;
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

    public void run(){
        if(device != null){
            if (device.getState() == activeDeviceState){
                device.changeState();
            }
            else{
                device.changeState(activeDeviceState);
            }
        }
    }

}
