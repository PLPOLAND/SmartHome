package smarthome.runners;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.i2c.JtAConverter;
import smarthome.model.hardware.Temperature;
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
    SystemDAO system;
    @Autowired
    JtAConverter converter;

    ArrayList<Termometr> termometrs;
    
    public Runners(){
        logger = LoggerFactory.getLogger(this.getClass());
        
    }

    // @Scheduled(fixedRate = 1000)
    void UpdateTemperature() {
        if (termometrs == null) {
            termometrs = system.getAllTermometers();
        }
        for (Termometr termometr : termometrs) {
            // converter.checkTemperature(termometr);
            logger.debug("Zaaktualizowano temperature termometra id="+termometr.getId()+", t="+termometr.getTemperatura());
        }
    }

}
