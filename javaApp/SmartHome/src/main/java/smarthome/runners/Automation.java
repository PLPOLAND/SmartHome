package smarthome.runners;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smarthome.automation.AutomationFunction;
import smarthome.database.AutomationDAO;
import smarthome.exception.HardwareException;
import smarthome.i2c.MasterToSlaveConverter;

@Service
public class Automation {
    Logger logger;

    @Autowired
    AutomationDAO automationDAO;

    @Autowired
    smarthome.system.System system;
    @Autowired
    MasterToSlaveConverter converter;

    ArrayList<AutomationFunction> functions;

    public Automation () {
        logger = LoggerFactory.getLogger(this.getClass());
        functions = new ArrayList<>();
    }


    @Scheduled(fixedRate = 1000)
    void checkAutomationFunctions(){
        logger.debug("checkAutomationFunctions");
        if (functions.size() != automationDAO.getAutomationFunctions().size()){
            functions.clear();
            functions.addAll(automationDAO.getAutomationFunctions());
        }
        for (AutomationFunction fun : functions){
            try {
                fun.run();
            } catch (HardwareException e) {
                logger.error("Error in automation function {}. Error: {}", fun.getId(), e.getMessage());
            }
        }
    }
}
