package smarthome.runners;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import smarthome.exception.HardwareException;
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

    //informujme o (nie)zakończeniu reinicjacji
    boolean isCheckReinitDone = true;
    //informuje o (nie)zakończeniu sprawdzania statusu urządzeń 
    boolean isCheckDevicesStatusDone = true;
    
    public Runners(){
        logger = LoggerFactory.getLogger(this.getClass());
        
    }

    // @Scheduled(fixedRate = 1000)
    // void updateTemperature() {
    //     if (termometrs == null) {
    //         termometrs = system.getSystemDAO().getAllTermometers();
    //     }
    //     for (Termometr termometr : termometrs) {
    //         system.updateTemperature(termometr);
    //         logger.debug("Zaaktualizowano temperature termometra id="+termometr.getId()+", t="+termometr.getTemperatura());
    //     }
    // }

    // @Scheduled(fixedDelay = 10000)
    void checkReinit(){
        if (isCheckReinitDone && isCheckDevicesStatusDone) {
            isCheckReinitDone = false;
            logger.debug("checkReinit()");
            system.reinitAllBoards();
            isCheckReinitDone = true;
        }
        
    }
    
    @Scheduled(fixedDelay = 10)
    void checkDevicesStatus(){
        if (!system.getArduino().atmega.getDevices().isEmpty()) {
            logger.debug("checkStatus()");
            if (isCheckDevicesStatusDone && isCheckReinitDone) {
                isCheckDevicesStatusDone = false;
                for (Device device : system.getSystemDAO().getDevices()) {
                    try {
                        system.checkInitOfBoard(device.getSlaveID());
                        system.updateDeviceState(device);
                    } catch (HardwareException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                for (Termometr termometr : system.getSystemDAO().getAllTermometers()) {
                    system.updateTemperature(termometr);
                }
                isCheckDevicesStatusDone = true;
            }
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
