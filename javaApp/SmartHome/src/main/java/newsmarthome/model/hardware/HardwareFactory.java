package newsmarthome.model.hardware;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;

/**
 * @author Marek Pałdyna
 * HardwareFactory - klasa fabryki urządzeń i sensorów. Umożliwia tworzenie obiektów pochodnych od device i sensors i autowireowania w nich pola Klasy MasterToSlaveConverter
 */
@Service("hardwareFactory")
public class HardwareFactory {
    
    @Autowired
    BeanFactory beanFactory;
    public HardwareFactory(){
        System.out.println("HardwareFactory");
    }

    public Light createLight(){
        return beanFactory.getBean(Light.class);
    }
    public Light createLight(int pin){
        return beanFactory.getBean(Light.class, pin);
    }
    public Light createLight(DeviceState stan, int pin, int slaveID){
        return beanFactory.getBean(Light.class, stan, pin, slaveID);
    }

    public Fan createFan(){
        return beanFactory.getBean(Fan.class);
    }
    public Fan createFan(int pin){
        return beanFactory.getBean(Fan.class, pin);
    }
    public Fan createFan(DeviceState stan, int pin, int slaveID){
        return beanFactory.getBean(Fan.class, stan, pin, slaveID);
    }

    public Outlet createOutlet(){
        return beanFactory.getBean(Outlet.class);
    }
    public Outlet createOutlet(int pin){
        return beanFactory.getBean(Outlet.class, pin);
    }
    public Outlet createOutlet(DeviceState stan, int pin, int slaveID){
        return beanFactory.getBean(Outlet.class, stan, pin, slaveID);
    }


    public Blind createBlind(){
        return beanFactory.getBean(Blind.class);
    }
    public Blind createBlind(int pinUp, int pinDown){
        return beanFactory.getBean(Blind.class, pinUp, pinDown);
    }
    public Blind createBlind(DeviceState stan, int pinUp, int pinDown, int slaveID){
        return beanFactory.getBean(Blind.class, stan, slaveID, pinUp, pinDown);
    }

    public Device createDevice(DeviceTypes type){
        switch(type){
            case LIGHT:
                return createLight();
            case WENTYLATOR:
                return createFan();
            case GNIAZDKO:
                return createOutlet();
            case BLIND:
                return createBlind();
            default:
                return null;
        }
    }


    // Sensors
    
    public Termometr createTermometr(){
        return beanFactory.getBean(Termometr.class);
    }

    public Button createButton(){
        return beanFactory.getBean(Button.class);
    }
    public Button createButton(int slaveID){
        return beanFactory.getBean(Button.class, slaveID);
    }
    public Button createButton(int pin, int slaveID){
        return beanFactory.getBean(Button.class, slaveID, pin);
    }

    public Sensor createSensor(SensorsTypes valueOf) {
        switch(valueOf){
            case BUTTON:
                return createButton();
            case THERMOMETR:
                return createTermometr();
            default:
                return null;
        }
    }
    



}
