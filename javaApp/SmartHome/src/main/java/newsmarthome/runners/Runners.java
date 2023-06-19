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
                    if (slaveSender.isSlaveConnected(device.getSlaveID())){
                        logger.debug("Slave {} is connected", device.getSlaveID());
                        logger.debug("Checking status of device {}", device);
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
                    if (e instanceof HardwareException) {
                        HardwareException he = (HardwareException) e;
                        if (he.getResponse() != null && he.getResponse()[0] == 'E') {
                            device.resetConfigured();
                            configureSlave(device.getSlaveID());
                        }
                    }
                }
            }
            for (Termometr termometr : systemDAO.getAllTermometers()) {
                if (slaveSender.isSlaveConnected(termometr.getSlaveAdress())) {
                    termometr.update();
                }
            }
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

    // @Scheduled(fixedDelay = 200)
    void checkAutomationFunctions() {
        if (!stopCheckingAutomation) {
            logger.debug("checkAutomationFunctions");
            if (functions.size() != automationDAO.getAutomationFunctions().size()) { // jeśli liczba funkcji zapisanych w systemie się zmieniła
                functions.clear(); // wyczyść listę funkcji
                functions.addAll(automationDAO.getAutomationFunctions()); // dodaj wszystkie funkcje z systemu do lokalnej listy
            }
            for (AutomationFunction fun : functions) { // dla każdej funkcji
                try {
                    fun.run(); // sprawdź czy jej warunki są spełnione i wykonaj ją
                } catch (HardwareException e) {
                    logger.error("Error in automation function {}. Error: {}", fun.getId(), e.getMessage());
                }
            }
            for (Integer slaveAdress : slaveSender.getSlavesAdresses()) {// dla każdego slave-a
                if (slaveSender.isSlaveConnected(slaveAdress)) { // jeśli jest podłączony
                    try {
                        if (!slaveSender.checkInitOfBoard(slaveAdress)){// sprawdź czy jest skonfigurowany
                            configureSlave(slaveAdress); // jeśli nie to wyślij mu konfigurację
                        } 
                        int howMany = slaveSender.howManyCommandToRead(slaveAdress); // sprawdź ile komend czeka na odczytanie
                        if (howMany > 0) { // jeśli są jakieś komendy w kolejce
                            for (int i = 0; i < howMany; i++) {
                                byte[] command = slaveSender.readCommandFromSlave(slaveAdress); // odczytaj komendę
                                if (command != null && command[0] == 'C') { // jeśli komenda jest komendą
                                    ButtonFunction buttonFunction = new ButtonFunction();
                                    buttonFunction.fromCommand(slaveAdress, command); // zainicjuj funkcję z danych z slave-a
                                    logger.debug("Pobrano z slave-a funkcję przycisku: {}", buttonFunction);

                                    for (ButtonFunction fun : automationDAO.getButtonFunctions()) { // dla każdej automatyki funkcji przycisku
                                        if (fun.compare(buttonFunction)) { // sprawdź czy funkcja zapisana w systemie jest taka sama jak ta pobrana z slave-a
                                            logger.debug("Znaleziono funkcję: {}", fun);
                                            fun.run();// jeśli tak to wykonaj ją
                                            break;// i przerwij dalsze sprawdzanie
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
        logger.debug("configureSlave({})", slaveAdress);
        try {
            if (slaveSender.reInitBoard(slaveAdress)) {
                logger.debug("Sending devices configuration to slave {}", slaveAdress);
                for (Device device : systemDAO.getDevices()) {
                    if (device.getSlaveID() == slaveAdress) {
                        device.resetConfigured();
                        device.configureToSlave();
                        try{
                            Thread.sleep(100);
                        }
                        catch(InterruptedException e){
                            logger.error("Błąd podczas usypiania wątku: {}", e.getMessage());
                        }
                    }
                }
                logger.debug("Sending sensors configuration to slave {}", slaveAdress);
                for (Sensor sensor : systemDAO.getSensors()) {
                    // jeśli sensor jest przyciskiem i jest na tym slave to wyślij jego konfigurację
                    // na slave'a
                    logger.debug("Checking sensor {} on slave {}", sensor, slaveAdress);
                    if (sensor.getSlaveAdress() == slaveAdress && sensor instanceof Button) {
                        Button button = (Button) sensor;
                        logger.debug("Sending button ({}) configuration to slave {}",button, slaveAdress);
                        button.configure();
                        try{
                            Thread.sleep(100);
                        }
                        catch(InterruptedException e){
                            logger.error("Błąd podczas usypiania wątku: {}", e.getMessage());
                        }
                    }

                }
            }
            logger.debug("Sending Thermometers configuration to slave {}", slaveAdress);
            // sprawdź i dodaj termometry
            int howManyTermometersAreOnSlave = slaveSender.howManyThermometersOnSlave(slaveAdress);
            ArrayList<Termometr> termometry = systemDAO.getAllTermometers();
            if (howManyTermometersAreOnSlave > 0) {
                for (int i = 0; i < howManyTermometersAreOnSlave; i++) {
                    int[] addres;
                    addres = slaveSender.addTermometr(slaveAdress);// dodaj termometr na slavie
                    //Sprawdź czy adres nie jest zawiera samych zer lub samych 255
                    boolean isCorrect= false;
                    for (int adr : addres) {
                        if (adr != 0 && adr != 255) {
                            isCorrect = true;
                            break;
                        }
                    }
                    if (!isCorrect) {
                        logger.error("Niepoprawny adres termometru: {}", Arrays.toString(addres));
                        continue;
                    }
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
        } catch (HardwareException e) {
            logger.error("Błąd podczas wysyłania konfiguracji na slave-a ({})! Error: {}", slaveAdress, e.getMessage());
            this.stopCheckingAutomation = false;
        }
        this.stopCheckingAutomation = false;
    }

}
