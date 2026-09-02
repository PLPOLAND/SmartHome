package newsmarthome.automation.conditions;

import org.springframework.stereotype.Component;

import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

@Component
public class DeviceCondition implements Condition{

    /** Urządzenie którego stan należy sprawdzić */
    private Device device;
    /** Stan urządzenia w którym warunek jest prawdziwy */
    private DeviceState activeDeviceState;
    
    static final ConditionType conditionType = ConditionType.DEVICE;

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

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }
    
}
