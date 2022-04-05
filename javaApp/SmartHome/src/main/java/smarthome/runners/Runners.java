package smarthome.runners;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import smarthome.i2c.JtAConverter;
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
    JtAConverter converter;

    ArrayList<Termometr> termometrs;
    
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

    @Scheduled(fixedRate = 60000)
    void checkReinit(){
        logger.debug("checkReinit()");
        system.reinitAllBoards();
    }

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
