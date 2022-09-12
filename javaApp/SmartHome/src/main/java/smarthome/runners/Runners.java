package smarthome.runners;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pi4j.io.i2c.I2CDevice;

import smarthome.database.AutomationDAO;
import smarthome.exception.HardwareException;
import smarthome.exception.SoftwareException;
import smarthome.i2c.MasterToSlaveConverter;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.Termometr;

/**
 * 
 * klasa zawierająca metody wykonywane co jakiś czas
 * 
 * @author Marek Pałdyna
 */
@Service
public class Runners {
    Logger logger;
    
    @Autowired
    smarthome.system.System system;
    @Autowired
    MasterToSlaveConverter converter;

    ArrayList<Termometr> termometrs;

    private static boolean stop = false;

    //informujme o (nie)zakończeniu reinicjacji
    boolean isCheckReinitDone = true;
    //informuje o (nie)zakończeniu sprawdzania statusu urządzeń 
    boolean isCheckDevicesStatusDone = true;
    
    public Runners(){
        logger = LoggerFactory.getLogger(this.getClass());
        
    }

    public static void pause(){
        Runners.stop = true;
    }

    public static void resume(){
        Runners.stop = false;
    }

    
    @Scheduled(fixedDelay = 500)
    void checkDevicesStatus(){
        if (!system.getArduino().atmega.getDevices().isEmpty()) {
            logger.debug("checkStatus()");
            if (!stop) {
                if (isCheckDevicesStatusDone && isCheckReinitDone) {
                    isCheckDevicesStatusDone = false;
                    for (Device device : system.getSystemDAO().getDevices()) {
                        if (system.isSlaveConnected(device.getSlaveID())) {
                            try {
                                system.checkInitOfBoard(device.getSlaveID());
                                system.updateDeviceState(device);
                            } catch (HardwareException | SoftwareException e) {
                                logger.error("{} {Device ID: {}}", e.getMessage(), device.getId());
                            }
                        }
                    }
                    for (Termometr termometr : system.getSystemDAO().getAllTermometers()) {
                        if (system.isSlaveConnected(termometr.getSlaveAdress())) {
                            system.updateTemperature(termometr);
                        }
                    }
                    for (I2CDevice device : system.getArduino().atmega.getDevices()) {
                        if (system.isSlaveConnected(device.getAddress())) {
                            system.checkGetAndExecuteCommandsFromSlave(device.getAddress());
                        }
                    }
                    isCheckDevicesStatusDone = true;
                }
            }
        } else {
            logger.debug("checkStatus is paused");
        }
    }

    
    // @Scheduled(fixedRate = 2000)
    // void updateDevicesState(){
    //     logger.debug("updateDevicesState()");
    //     for (Device device : system.getSystemDAO().getDevices()) {
    //         try{
    //             system.updateDeviceState(device);
    //         }catch(HardwareException e){
    //             logger.error(e.getMessage(), e);;
    //         }
    //     }
    // }

    /* KOD do wykrywania stanu na GPIO


			final GpioController gpio = GpioFactory.getInstance();
			final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
			input.addListener(new GpioPinListenerDigital() {

				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					System.out.println(event.getPin() + " = " + event.getState());

					//Wenn der Pin#2 auf High geht, fährt sich der Rasperry Pi runter.
					// if (input.getState()==PinState.HIGH) {
					// 	try {
					// 		Process p = Runtime.getRuntime().exec("echo tmp");
					// 		p.waitFor();
					// 	} catch (IOException | InterruptedException e) {
					// 		e.printStackTrace();
					// 	}
					// }

				}
			});

    */
}
