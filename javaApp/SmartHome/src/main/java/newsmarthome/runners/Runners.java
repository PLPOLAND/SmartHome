package newsmarthome.runners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import newsmarthome.automation.AutomationFunction;
import newsmarthome.automation.ButtonFunction;
import newsmarthome.database.AutomationDAO;
import newsmarthome.database.SystemDAO;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.sensor.Button;
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
    AutomationDAO automationDAO;

    /** Tymczasowo przechowuje funkcje automatyki */
    ArrayList<AutomationFunction> functions = new ArrayList<>();

    /** Zatrzymuje sprawdzanie automatyki*/
    private boolean stopCheckingAutomation = false; 

    /**
     * Sprawdza czy slave jest podłączony i czy jest skonfigurowany. Jeśli tak to sprawdza stan urządzeń i sensorów
     */
    @Scheduled(fixedDelay = 500)
    void statusCheck(){
        
        List<Integer> slaves = slaveSender.getSlavesAdresses();
        if (!slaves.isEmpty()){
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
                            configureSlave(device.getSlaveID());
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
            logger.warn("No slaves detected");
            int time = 1000;
            while (slaves.isEmpty()){
                try {
                    Thread.sleep(time);
                    if (time <60000) {
                        time += 1000;
                    }
                    logger.info("Searching for slaves...");
                    slaveSender.findSlaves();
                    
                } catch (InterruptedException e) {
                    logger.error("Błąd podczas usypiania wątku: {}", e.getMessage());
                }
                slaves = slaveSender.getSlavesAdresses();
            }
        }
    }

    @Scheduled(fixedDelay = 200)
    void checkAutomationFunctions() {
        if (!stopCheckingAutomation) {
            logger.debug("checkAutomationFunctions");
            if (functions.size() != automationDAO.getAutomationFunctions().size()) {
                functions.clear();
                functions.addAll(automationDAO.getAutomationFunctions());
            }
            for (AutomationFunction fun : functions) {
                try {
                    fun.run();
                } catch (HardwareException e) {
                    logger.error("Error in automation function {}. Error: {}", fun.getId(), e.getMessage());
                }
            }
            for (Integer slaveAdress : slaveSender.getSlavesAdresses()) {
                if (slaveSender.isSlaveConnected(slaveAdress)) {
                    try {
                        slaveSender.checkInitOfBoard(slaveAdress);
                        // slaveSender.checkGetAndExecuteCommandsFromSlave(slaveAdress);
                        int howMany = slaveSender.howManyCommandToRead(slaveAdress);
                        if (howMany > 0) {
                            for (int i = 0; i < howMany; i++) {
                                byte[] command = slaveSender.readCommandFromSlave(slaveAdress);
                                if (command != null && command[0] == 'C') {
                                        ButtonFunction buttonFunction = new ButtonFunction();
                                        buttonFunction.fromCommand(slaveAdress, command); // zainicjuj funkcję z danych z slave-a
                                        logger.debug("Pobrano z slave-a funkcję przycisku: {}", buttonFunction);

                                        for (ButtonFunction fun : automationDAO.getButtonFunctions()) {
                                            if (fun.compare(buttonFunction)) {
                                                logger.debug("Znaleziono funkcję: {}", fun);
                                                fun.run();
                                                break;
                                            }

                                        }
                                }
                            }
                        }
                    } catch (HardwareException | SoftwareException e) {
                        logger.error("Error in checkAutomationFunctions. Error: {}", e.getMessage());
                    }
                }
            }
        } else {
            logger.debug("checkAutomationFunctions is paused");
        }
    }


    /**
     * Wysyła konfigurację slave'owi. Zatrzymuje sprawdzanie automatyki na czas wysyłania konfiguracji.
     * 
     * @param slaveAdress - adres slave'a na który ma zostać wysłana konfiguracja
     */
    private void configureSlave(int slaveAdress) {
        this.stopCheckingAutomation = true;
        try {
            if (slaveSender.checkAndReinitBoard(slaveAdress)) {
                for (Device device : systemDAO.getDevices()) {
                    if (device.getSlaveID() == slaveAdress) {
                        device.resetConfigured();
                        device.configureToSlave();
                    }

                }
                for (Sensor sensor : systemDAO.getSensors()) {
                    // jeśli sensor jest przyciskiem i jest na tym slave to wyślij jego konfigurację
                    // na slave'a
                    if (sensor.getSlaveAdress() == slaveAdress && sensor instanceof Button) {
                        Button button = (Button) sensor;
                        button.configure();
                    }

                }
            }
            // sprawdź i dodaj termometry
            int howManyTermometersAreOnSlave = slaveSender.howManyThermometersOnSlave(slaveAdress);
            ArrayList<Termometr> termometry = systemDAO.getAllTermometers();
            if (howManyTermometersAreOnSlave > 0) {
                for (int i = 0; i < howManyTermometersAreOnSlave; i++) {
                    int[] addres;
                    addres = slaveSender.addTermometr(slaveAdress);// dodaj termometr na slavie
                    boolean existed = false;// czy ten termometr był już dodany w systemie

                    for (Termometr t : termometry) {// wśród wszystkich dodanych w systemie
                        if (Arrays.equals(t.getAddres(), addres)) {// znajdź ten który został właśnie dodany
                            t.setSlaveAdress(slaveAdress);// zmień mu adres slave-a
                            existed = true;
                            break;
                        }
                    }
                    if (!existed) {
                        Termometr termometr = new Termometr(slaveAdress);
                        termometr.setAddres(addres);
                        termometr.setName("Dodany automatycznie, slave=" + slaveAdress);
                        Room tmp = systemDAO.getRoom("Brak");
                        if (tmp != null) {

                            termometr.setRoom(tmp.getID());
                            tmp.addSensor(termometr);
                            systemDAO.save(tmp);
                        } else {
                            logger.error("Nie znaleziono pokoju '{}' podczas dodawania nowego termometru  ", "Brak");
                        }
                    }
                }
            }
        } catch (SoftwareException | HardwareException e) {
            logger.error("Błąd podczas wysyłania konfiguracji na slave-a ({})! Error: {}", slaveAdress, e.getMessage());
            this.stopCheckingAutomation = false;
        }
        this.stopCheckingAutomation = false;
    }

}
