package newsmarthome.runners;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import newsmarthome.database.SystemDAO;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.Termometr;
import newsmarthome.model.Room;

@Service
public class System { //TODO zmienić nazwę na odpowiednią i przenieść do odpowiedniego pakietu
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SystemDAO systemDAO;
    @Autowired
    private MasterToSlaveConverter slaveSender;

    /**
     * Wysyła konfigurację slave'owi
     * 
     * @param slaveAdress
     */
    public void configureSlave(int slaveAdress) {
        try {
            if (slaveSender.checkAndReinitBoard(slaveAdress)) {
                for (Device device : systemDAO.getDevices()) {
                    if (device.getSlaveID() == slaveAdress) {
                        device.resetConfigured();
                        device.configureToSlave();
                    }

                }
                for (Sensor sensor : systemDAO.getSensors()) {
                    //jeśli sensor jest przyciskiem i jest na tym slave to wyślij jego konfigurację na slave'a
                    if (sensor.getSlaveAdress() == slaveAdress && sensor instanceof Button) {
                        Button button = (Button) sensor;
                        button.configure();
                    }

                }
            }
            //sprawdź i dodaj termometry
            int howManyTermometersAreOnSlave = slaveSender.howManyThermometersOnSlave(slaveAdress);
            ArrayList<Termometr> termometry = systemDAO.getAllTermometers();
            if (howManyTermometersAreOnSlave > 0) {
                for (int i = 0; i < howManyTermometersAreOnSlave; i++) {
                int[] addres;
                addres = slaveSender.addTermometr(slaveAdress);//dodaj termometr na slavie
                boolean existed = false;//czy ten termometr był już dodany w systemie

                for (Termometr t : termometry) {//wśród wszystkich dodanych w systemie 
                    if (Arrays.equals(t.getAddres(), addres)) {//znajdź ten który został właśnie dodany
                        t.setSlaveAdress(slaveAdress);//zmień mu adres slave-a
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
                    }
                    else{
                        logger.error("Nie znaleziono pokoju '{}' podczas dodawania nowego termometru  ","Brak");
                    }
                }
            }
            }
        } catch (SoftwareException | HardwareException e) {
            logger.error("Błąd podczas wysyłania konfiguracji na slave-a ({})! Error: {}", slaveAdress, e.getMessage());
        }
    }

}
