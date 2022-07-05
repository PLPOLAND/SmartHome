package smarthome.automation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import smarthome.SmartHomeApp;
import smarthome.exception.HardwareException;
import smarthome.i2c.MasterToSlaveConverter;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

@Component
public class FunctionAction {

    @Autowired
    private static MasterToSlaveConverter slave;

    private Device device;
    private DeviceState activeDeviceState;
    boolean allowReverse;//true - można odwrócić stan urządzenia podczas wykonywania akcji

    public FunctionAction(){
        device = null;
        activeDeviceState = DeviceState.NOTKNOW;
        allowReverse = false;
    }
    public FunctionAction(Device device, DeviceState activeDeviceState, boolean allowReverse){
        this.device = device;
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

        changeStateOnSlave();
    }

    /**
     * Zmienia stan urządzenia do stanu przeciwnego niż zapisany w activeDeviceState
     * 
     * @throws HardwareException
     * @throws IllegalArgumentException kiedy typ urządzenia nie jest jeszcze zaimplementowany
     */
    public void deactivate() throws HardwareException, IllegalArgumentException{
        device.changeToOppositeState(activeDeviceState);
        changeStateOnSlave();
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

            changeStateOnSlave();
        }
    }

    /**
     * Zmień stan urządzenia również na slavie.
     * @throws HardwareException
     */
    private void changeStateOnSlave() throws HardwareException {
        if (FunctionAction.slave == null) {
            FunctionAction.setSlave(SmartHomeApp.getApp().getBean(MasterToSlaveConverter.class));
        }
        switch (device.getTyp()) {
            case LIGHT:
                FunctionAction.slave.changeSwitchState(device.getOnSlaveID(), device.getSlaveID(), device.getState());
                break;
            case BLIND:
                FunctionAction.slave.changeBlindState((Blind) device, device.getState());
                break;
            default:
                throw new IllegalArgumentException("Urządzenie tego typu nie zostało jeszcze zaimplementowane w funkcji activate()");
        }
    }

}
