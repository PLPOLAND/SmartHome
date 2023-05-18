package newsmarthome.runners;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import newsmarthome.database.SystemDAO;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.Termometr;

@Service
public class Runners {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SystemDAO systemDAO;
    @Autowired
    MasterToSlaveConverter slaveSender;
    @Autowired
    System system;

    @Scheduled(fixedDelay = 500)
    
    void statusCheck(){
        
        List<Integer> devices = slaveSender.getSlavesAdresses();
        if (!devices.isEmpty()){
            for(Device device : systemDAO.getDevices()){
                try {
                    logger.debug("Checking status of device {}", device);
                    if (slaveSender.isSlaveConnected(device.getSlaveID())){
                        logger.debug("Slave {} is connected", device.getSlaveID());
                        if (slaveSender.checkInitOfBoard(device.getSlaveID())) {
                            device.setConfigured();
                            device.updateDeviceState();
                        }
                        else{
                            device.resetConfigured();
                            system.configureSlave(device.getSlaveID());
                        }
    
                    }
                    else{
                        device.resetConfigured();//jako, że slave nie jest podłączony, to urządznie nie jest na nim skonfigurowane
                    }
                } catch (HardwareException|SoftwareException e) {
                    logger.error("Bład podczas sprawdzania stanu urządzenia {}: {}", device.getId(), e.getMessage());
                }
            }
            for (Termometr termometr : systemDAO.getAllTermometers()) {
                if (slaveSender.isSlaveConnected(termometr.getSlaveAdress())) {
                    termometr.update();
                }
            }
            // for (Integer device : slaveSender.getSlavesAdresses()) { //TODO automatyka
            //     if (slaveSender.isSlaveConnected(device)) {
            //         system.checkGetAndExecuteCommandsFromSlave(device);
            //     }
            // }
        }
        else{
            logger.warn("No devices connected");
            while (devices.isEmpty()){
                try {
                    Thread.sleep(1000);
                    logger.info("Searching for slaves");
                    slaveSender.findSlaves();
                    
                } catch (InterruptedException e) {
                    logger.error("Błąd podczas usypiania wątku: {}", e.getMessage());
                }
                devices = slaveSender.getSlavesAdresses();
            }
        }
    }

}
