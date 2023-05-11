package newsmarthome.model.hardware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;

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
                return context.getBean(Light.class);
            case GNIAZDKO:
                return context.getBean(Outlet.class);
            case BLIND:
                return context.getBean(Blind.class);
            case WENTYLATOR:
                return context.getBean(Fan.class);
            default:
            throw new IllegalArgumentException("Nie prawidłowy typ urządzenia. Podany typ = " + type);
        }
    }


}
