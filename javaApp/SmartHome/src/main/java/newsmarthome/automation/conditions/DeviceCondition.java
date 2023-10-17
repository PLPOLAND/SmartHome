package newsmarthome.automation.conditions;

import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

public class DeviceCondition implements Condition{

    /** Urządzenie którego stan należy sprawdzić */
    Device device;
    /** Stan urządzenia w którym warunek jest prawdziwy */
    DeviceState activeDeviceState;

    public DeviceCondition(Device device, DeviceState activeDeviceState) {
        this.device = device;
        this.activeDeviceState = activeDeviceState;
    }

    public DeviceCondition() {
        this.device = null;
        this.activeDeviceState = DeviceState.NOTKNOW;
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
    @Override
    public boolean checkCondition() {
        return device.getState() == activeDeviceState;
    }
    
}
