package newsmarthome.runners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
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
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.Higrometr;
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

    @Autowired
    BeanFactory beanFactory; //potrzebne do tworzenia nowych obiektów w trakcie działania programu

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
                        // logger.debug("Slave {} is connected", device.getSlaveID());
                        logger.debug("Checking status of device (id: {}, name:{}, type: {}, configured: {}) ", device.getId(), device.getName(), device.getTyp(), device.isConfigured());
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
                        logger.debug("Slave {} is not connected", device.getSlaveID());
                        device.resetConfigured();//jako, że slave nie jest podłączony, to urządznie nie jest na nim skonfigurowane
                    }
                } catch (HardwareException e) {
                    logger.error("Bład podczas sprawdzania stanu urządzenia o id: {}: {}", device.getId(), e.getMessage());
                        // if (e.getResponse() != null && e.getResponse()[0] == 'E') {
                        //     try{
                        //         Thread.sleep(100);
                        //         device.updateDeviceState();
                        //     }
                        //     catch(InterruptedException ex){
                        //         logger.error("Błąd podczas usypiania wątku: {}", ex.getMessage());
                        //     }
                        //     catch(HardwareException ex){
                        //     device.resetConfigured();
                        //     configureSlave(device.getSlaveID());
                        //     }
                        //     catch(SoftwareException ex){
                        //         logger.error("Bład podczas sprawdzania stanu urządzenia o id: {}: {}", device.getId(), ex.getMessage());
                        //         if (ex.getExpected() != null ) {
                        //             if (device instanceof Light || device instanceof Fan || device instanceof Outlet){
                        //                 logger.debug("Trying to turn off device");
                        //                 device.changeState(DeviceState.OFF);
                        //             } 
                        //             else if (device instanceof Blind){
                        //                 logger.debug("Trying to stop blind");
                        //                 device.changeState(DeviceState.UP);
                        //                 device.changeState(DeviceState.NOTKNOW);
                        //             }
                        //         }
                        //         else{
                        //             logger.warn("Brak oczekiwanych odpowiedzi od slave'a. Zgłoś błąd do administratora. Error: {}", Arrays.toString(ex.getStackTrace()));
                        //         }
                        //     }
                        // }
                   
                }
                catch(SoftwareException e){
                    logger.error("Bład podczas sprawdzania stanu urządzenia o id: {}: {}", device.getId(), e.getMessage());
                        if (e.getExpected() != null ) {
                            if (device instanceof Light || device instanceof Fan || device instanceof Outlet){
                                logger.debug("Trying to turn off device");
                                device.changeState(DeviceState.OFF);
                            } 
                            else if (device instanceof Blind){
                                logger.debug("Trying to stop blind");
                                device.changeState(DeviceState.UP);
                                device.changeState(DeviceState.NOTKNOW);
                            }
                        }
                        else{
                            logger.warn("Brak oczekiwanych odpowiedzi od slave'a. Zgłoś błąd do administratora. Error: {}", Arrays.toString(e.getStackTrace()));
                        }
                }
            }
            for (Termometr termometr : systemDAO.getAllTermometers()) {
                if (slaveSender.isSlaveConnected(termometr.getSlaveAdress())) {
                    termometr.update();
                }
            }
            for (Higrometr higrometr : systemDAO.getAllHigrometers()) {
                try{
                    if (slaveSender.isSlaveConnected(higrometr.getSlaveAdress())) {
                        if (slaveSender.checkInitOfBoard(higrometr.getSlaveAdress())) {
                            higrometr.update();
                        }
                        else{
                            configureSlave(higrometr.getSlaveAdress());
                        }
                    }

                }
                catch (HardwareException|SoftwareException e) {
                    logger.error("Bład podczas sprawdzania stanu higrometru o id: {}: {}", higrometr.getId(), e.getMessage());
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

    @Scheduled(fixedDelay = 200)
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
                                    ButtonFunction buttonFunction = beanFactory.getBean(ButtonFunction.class);
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
        logger.info("Send configuration to Slave({})", slaveAdress);
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
                    // jeśli sensor jest przyciskiem lub higrometrem i jest na tym slave to wyślij jego konfigurację
                    // na slave'a
                    if (sensor.getSlaveAdress() == slaveAdress && sensor instanceof Button) {
                        Button button = (Button) sensor;
                        logger.debug("Sending button (id:{}) configuration to slave {}",button.getId(), slaveAdress);
                        button.configure();
                        try{
                            Thread.sleep(10);
                        }
                        catch(InterruptedException e){
                            logger.error("Błąd podczas usypiania wątku: {}", e.getMessage());
                        }
                    }
                    else if(sensor.getSlaveAdress() == slaveAdress && sensor instanceof Higrometr){
                        Higrometr higrometr = (Higrometr) sensor;
                        logger.debug("Sending higrometr (id:{}) configuration to slave {}",higrometr.getId(), slaveAdress);
                        higrometr.configure();
                        try{
                            Thread.sleep(10);
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
                    if (!checkAdress(addres)) {
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
                        termometr.setNazwa("Dodany automatycznie, slave=" + slaveAdress);
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
     /**
      * Sprawdź czy adres nie jest zawiera samych zer lub samych 255
      */
    private boolean checkAdress(int[] addres) {
        boolean isCorrect= false;
        for (int adr : addres) {
            if (adr != 0 && adr != 255) {
                isCorrect = true;
                break;
            }
        }
        return isCorrect;
    }

}
