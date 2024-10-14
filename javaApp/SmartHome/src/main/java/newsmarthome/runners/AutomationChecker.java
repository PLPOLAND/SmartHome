package newsmarthome.runners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import newsmarthome.database.AutomationDAO;
import newsmarthome.exception.HardwareException;


@Service
@Log4j2
public class AutomationChecker {
    
    
    private AutomationDAO automationDAO;
    AutomationChecker( @Autowired AutomationDAO automationDAO){
        this.automationDAO = automationDAO;
    }

    @Scheduled(fixedRate = 1)
    public void checkAutomations() {
        automationDAO.getAutomationFunctions().forEach(automation -> {
            try {
                automation.run();
            } catch (HardwareException e) {
                log.error("Hardware Error while running automation: " + automation.getName(), e);
            }
        });
        
    }
}
