package smarthome.automation;

import smarthome.SmartHomeApp;
import smarthome.exception.HardwareException;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonClickType;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;
import smarthome.system.System;

/**
 * ButtonFunction
 */
public class ButtonFunction extends Function{

    private static System system;

    Button button;
    int clicks;
    ButtonClickType clickType;

    public ButtonFunction() {
        super();
        reversState = true;
        button = null;
        clicks = 0;
        clickType = null;
    }

    public ButtonFunction(Button button, int clicks, ButtonClickType clickType) {
        super();
        reversState = true;
        this.button = button;
        this.clicks = clicks;
        this.clickType = clickType;
    }


    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public ButtonClickType getClickType() {
        return clickType;
    }

    public void setClickType(ButtonClickType clickType) {
        this.clickType = clickType;
    }

    public void addAction(Device device, DeviceState activeDeviceState) {
        addAction( device, activeDeviceState, true);
    }

    /**
     * Pobiera bean systemu z aplikacji i ustawia go.
     */
    private static void setSystem() {
        if (ButtonFunction.system == null) {
            ButtonFunction.system  = SmartHomeApp.getApp().getBean(System.class);
        }
    }
    /**
     * Inicjalizuje obiekt na podstawie danych otrzymanych z slave.
     * @param slaveAdress - adres slave-a z którego przyszła komenda.
     * @param command - komenda z slave-a. (tablica nie mniejsza niż 4 elementy)
     */
    public void fromCommand(int slaveAdress, byte[] command) {
        setSystem();
        button = (Button) system.getSensorByOnSlaveID(slaveAdress, command[1]);
        clicks = command[2];
        switch (command[3]) {
            case 'P':
                clickType = ButtonClickType.HOLDED;
                break;
            case 'C':
                clickType = ButtonClickType.CLICKED;
                break;
            case 'H':
                clickType = ButtonClickType.HOLDING;
                break;
            default:
                break;
        }
    }
    /**
     * Porównuje rodzaj kliknięcia, ilość kliknięć i przycisk i zwraca true jeśli są takie same.
     * @param fun - funkcja do porównania.
     * @return true jeśli są takie same, false w przeciwnym wypadku.
     */
    public boolean compare(ButtonFunction fun) {
        return fun.getClickType() == getClickType() && fun.getClicks() == getClicks() && fun.getButton().equals(button);
    }

    @Override
    public void run() throws HardwareException {
        
        for (FunctionAction action : actions) {
            action.run();
        }
        
    }

    @Override
    public void activate() throws HardwareException {
        
        for (FunctionAction action : actions) {
            action.activate();
        }
    }

    @Override
    public void deactivate() throws HardwareException {
        for (FunctionAction action : actions) {
            action.deactivate();
        }
    }


    @Override
    public String toString() {
        return "{" +
            " button='" + getButton().toString() + "'" +
            ", clicks='" + getClicks() + "'" +
            ", clickType='" + getClickType() + "'" +
            "}";
    }
    
}