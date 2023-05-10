package newsmarthome.model.hardware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;

@Service
public class HardwareFactory {
    
    @Autowired
    ApplicationContext context;
    public HardwareFactory(){
        System.out.println("HardwareFactory");
    }

    public Device createDevice(DeviceTypes type){
        switch (type){
            case LIGHT:
                // return new Light();
                return context.getBean(Light.class);
            case GNIAZDKO:
                // return new ;
            case BLIND:
                // return new Blind();
                return context.getBean(Blind.class);
            case WENTYLATOR:
                // return new TemperatureSensor();
            default:
            throw new IllegalArgumentException("Nie prawidłowy typ urządzenia. Podany typ = " + type);
        }
    }


}
