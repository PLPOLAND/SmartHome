package newsmarthome.model.hardware;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;

@Service
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
}
