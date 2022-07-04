package smarthome.automation;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import smarthome.exception.HardwareException;
import smarthome.i2c.MasterToSlaveConverter;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;

@Component
public class FunctionAction {

    @Autowired
    private MasterToSlaveConverter slave;

    private Device device;
    private DeviceState activeDeviceState;
    boolean allowReverse;//true - można odwrócić stan urządzenia podczas wykonywania akcji

    public FunctionAction(){
        device = null;
        activeDeviceState = DeviceState.NOTKNOW;
        allowReverse = false;
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    public FunctionAction(Device device, DeviceState activeDeviceState, boolean allowReverse){
        this.device = device;
        this.activeDeviceState = activeDeviceState;
        this.allowReverse = allowReverse;
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
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
    
    public boolean isAllowReverse() {
        return allowReverse;
    }

    public void setAllowReverse(boolean allowReverse) {
        this.allowReverse = allowReverse;
    }

    public boolean getAllowReverse() {
        return allowReverse;
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
        if (slave == null) {
            System.out.println("slave is null");
        }
        switch (device.getTyp()) {
            case LIGHT:
                slave.changeSwitchState(device.getOnSlaveID(), device.getSlaveID(), device.getState());
                break;
            case BLIND:
                slave.changeBlindState((Blind) device, device.getState());
                break;
            default:
                throw new IllegalArgumentException("Urządzenie tego typu nie zostało jeszcze zaimplementowane w funkcji activate()");
        }
    }

}
