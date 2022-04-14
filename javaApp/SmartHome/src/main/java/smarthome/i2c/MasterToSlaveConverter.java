package smarthome.i2c;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.pi4j.io.serial.Serial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.exception.HardwareException;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Switch;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonFunction;
import smarthome.model.hardware.Sensor;
import smarthome.model.hardware.SensorsTypes;
import smarthome.model.hardware.Termometr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Klasa odpowiadająca za kompunikację pomiędzy Masterem a Slave-ami
 * 
 * @author Marek Pałdyna
 */
@Service
public class MasterToSlaveConverter {

    private static final int MAX_ROZMIAR_ODPOWIEDZI = 8;
    // #region Komendy
    /**[S,U]*/
    final byte[] STATUS_URZADZEN = { 'S', 'U' };
    /**[S,R]*/
    final byte[] STATUS_RGB = { 'S', 'R' };
    /**[W]*/
    final byte[] CHECK_TO_WORK = { 'W' };
    /**[I]*/
    final byte[] CHECK_INIT = { 'I' };
    /**[R]*/
    final byte[] REINIT = { 'R' };
    /**[U,S]*/
    final byte[] ZMIEN_STAN_PRZEKAZNIKA = { 'U' , 'S'}; // + id + stan
    /**[U,B] */
    final byte[] ZMIEN_STAN_ROLETY = { 'U' , 'B'}; // + id + stan
    /**[T]*/
    final byte[] POBIERZ_TEMPERATURE = { 'T' }; // + ADRESS (8byte)
    /**[A, S]*/
    final byte[] DODAJ_URZADZENIE = { 'A', 'S' }; // + PIN
    /**[A, R]*/
    final byte[] DODAJ_ROLETE = { 'A', 'R' }; // + PIN + PIN
    /**[A, P]*/
    final byte[] DODAJ_PRZYCISK = { 'A', 'P' }; // + PIN
    /**[A, T]*/
    final byte[] DODAJ_TERMOMETR = { 'A', 'T' };
    /**[P, K, L] */
    final byte[] DODAJ_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L' };
    /**[P, K, L, D] */
    final byte[] USUN_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L', 'D' };
    // #endregion

    @Autowired
    public I2C atmega;

    @Autowired
    SystemDAO system;

    /** Logger Springa */
    Logger logger;

    MasterToSlaveConverter() {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Stworzno JtAConverter");
    }

    /**
     * Zmien stan przekaznika
     * 
     * @param przekaznik - przekaznik docelowy
     * @param stan       - stan przekaznika
     */
    public void changeSwitchState(int idPrzekaznika, int idPlytki, boolean stan) throws HardwareException {
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_PRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) idPrzekaznika;
        buffor[i++] = (byte) (stan == true ? 1 : 0);
        atmega.writeTo(idPlytki, buffor);
        byte[] response = atmega.readFrom(idPlytki, 8);//TODO obsluga bledu
        logger.debug(Arrays.toString(response));
    }

    public void changeBlindState(Blind roleta, boolean stan) {
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_ROLETY) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) roleta.getOnSlaveID();
        if (stan) {
            buffor[i++] = 'U';
            logger.debug("Wysyłanie komendy podniesienia Rolety");
        }
        else{
            buffor[i++] = 'D';
            logger.debug("Wysyłanie komendy opuszczenia Rolety");
        }
        try {
            atmega.writeTo(roleta.getSlaveID(), buffor);
            byte[] response = atmega.readFrom(roleta.getSlaveID(), 8);//TODO obsluga bledu
            logger.debug(Arrays.toString(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sprawdz i zaaktualizuj temperaturę dla podanego termometra
     * 
     * @param termometr - termometr docelowy
     */
    public Float checkTemperature(Termometr termometr) {
        byte[] buffor = new byte [9];
        String bufString = "";

        int i =0;
        for (byte b : POBIERZ_TEMPERATURE) {
            buffor[i++] = b;
            bufString += (int)b + " ";
        }
        for (int adr : termometr.getAddres()) {
            buffor[i++] = (byte) adr;
            bufString += adr + " ";
        }
        try {
            logger.debug("Writing to addres " + termometr.getSlaveID() + " command: " + Arrays.toString(buffor));
            atmega.writeTo(termometr.getSlaveID(), buffor);
            Thread.sleep(100);
            byte[] response = atmega.readFrom(termometr.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);
            String tmp ="";
            for (byte b : response) {
                if (b >= 48 && b<= 57 || b == '.') {
                    tmp += (char) b;
                }
            }
            Float temperatura = Float.parseFloat(tmp);

            termometr.setTemperatura(temperatura);

            return temperatura;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -128.f;
        }

    }

    /**
     * Wysyła komendę dodającą nowe urządzenia (LIGHT/GNIAZDKO)
     * @param device urządzenie do dodania
     * @return id na płytce (-1 jeśli nie powiodło się)
     */
    public int addUrzadzenie(Device device) throws HardwareException{
        if (device.getTyp() == DeviceTypes.LIGHT || device.getTyp() == DeviceTypes.GNIAZDKO) {
            byte[] buffor = new byte[3];
            int i = 0;
            for (byte b : DODAJ_URZADZENIE) {
                buffor[i++] = b;
            }
            if(device.getTyp() == DeviceTypes.LIGHT){
                buffor[i++] = (byte) (((Light) device).getPin());
            }
            else{
                // buffor[i++] = (byte) ((() device).getPin());//TODO add Gniazdko
            }

            try {
                logger.debug("Writing to addres {}", device.getSlaveID());
                atmega.writeTo(device.getSlaveID(), buffor);
                Thread.sleep(100);//TODO czy jest potrzebne?
                logger.debug("Reading from addres {}", device.getSlaveID());
                byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                return response[0];
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                logger.debug("Próba kontynuacji");
                logger.debug("Reading from addres {}", device.getSlaveID());
                byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                return response[0];
            }
        }
        if(device.getTyp() == DeviceTypes.BLIND){
            byte[] buffor = new byte[4];
            int i = 0;
            for (byte b : DODAJ_ROLETE) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) (((Blind) device).getPinUp());
            buffor[i++] = (byte) (((Blind) device).getPinDown());
            
            try {
                logger.debug("Writing to addres {}", device.getSlaveID());
                atmega.writeTo(device.getSlaveID(), buffor);
                Thread.sleep(100);
                byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//TODO: dodawanie przekaźników o id podanym w odpowiedzi!
                return response[0];
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                logger.debug("Próba kontynuacji");
                logger.debug("Reading from addres {}", device.getSlaveID());
                byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                return response[0];
            }
        }

        return -1;
    }

    public byte[] addTermometr(Sensor sens) {
        if (sens.getTyp() == SensorsTypes.THERMOMETR) {
            byte[] buffor = new byte[2];
            int i = 0;
            for (byte b : DODAJ_TERMOMETR) {
                buffor[i++] = b;
            }
            try {
                atmega.writeTo(sens.getSlaveID(), buffor);//Wyślij prośbę o dodanie nowego termometru na płytce
                Thread.sleep(200);
                buffor = atmega.readFrom(sens.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);
                return buffor;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        return null;
    }

    public int addPrzycisk(Button button)throws HardwareException{
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : DODAJ_PRZYCISK) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) button.getPin();
        

        try {
            logger.debug("Writing to addres {}", button.getSlaveID());
            atmega.writeTo(button.getSlaveID(), buffor);
            Thread.sleep(100);// TODO czy jest potrzebne?
            logger.debug("Reading from addres {}", button.getSlaveID());
            byte[] response = atmega.readFrom(button.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            logger.debug("Próba kontynuacji");
            logger.debug("Reading from addres {}", button.getSlaveID());
            byte[] response = atmega.readFrom(button.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        }
    }
    /**
     * Wysyła funkcję kilknięć lokalną
     * @param function - funkcja do wysłania
     * @return
     * @throws HardwareException
     */
    public int sendClickFunction(ButtonFunction function) throws HardwareException{
        byte[] buffor = new byte[7];
        byte[] tmp2 = function.toCommand();
        int i = 0;
        for (byte b : DODAJ_LOKALNA_FUNKCJE_KLIKNIEC) {
            buffor[i++] = b;
        }
        buffor[i++] = tmp2[0];
        buffor[i++] = tmp2[1];
        buffor[i++] = tmp2[2];
        buffor[i] = tmp2[3];

        try {
            logger.debug("Writing to addres {}", function.getButton().getSlaveID());
            atmega.writeTo(function.getButton().getSlaveID(), buffor);
            Thread.sleep(100);// TODO czy jest potrzebne?
            logger.debug("Reading from addres {}", function.getButton().getSlaveID());
            byte[] response = atmega.readFrom(function.getButton().getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            logger.debug("Próba kontynuacji");
            logger.debug("Reading from addres {}", function.getButton().getSlaveID());
            byte[] response = atmega.readFrom(function.getButton().getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        }

    }



    public int sendRemoveFunction(int slaveID, int numberOfClicks) throws HardwareException{
        byte[] buffor = new byte[5];
        int i = 0;
        for (byte b : USUN_LOKALNA_FUNKCJE_KLIKNIEC) {
            buffor[i++] = b;
        }
        buffor[i] = (byte)numberOfClicks;

        try {
            logger.debug("Writing to addres {}", slaveID);
            atmega.writeTo(slaveID, buffor);
            Thread.sleep(100);// TODO czy jest potrzebne?
            logger.debug("Reading from addres {}", slaveID);
            byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            logger.debug("Próba kontynuacji");
            logger.debug("Reading from addres {}", slaveID);
            byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
            return response[0];
        }

    }


    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany
     * @param adres - adres slave-a który zostanie zapytany
     * @return stan zainicjowania slave-a
     */
    public boolean checkInitOfBoard(int adres){
        byte[] buffor = new byte[1];
        int i = 0;
        for (byte b : CHECK_INIT) {
            buffor[i++] = b;
        }

        try {
            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            Thread.sleep(200);
            buffor = atmega.readFrom(adres, MAX_ROZMIAR_ODPOWIEDZI);
            return buffor[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Wysyła komendę do slave-a po której slave usuwa wszystkie zapisane u siebie urządzenia
     * @param adres - adres slave-a na który zostanie wysłana komenda
     * @return true jeśli slave odpowie, że dostał komendę
     */
    public boolean reInitBoard(int adres) {
        byte[] buffor = new byte[1];
        int i = 0;
        for (byte b : REINIT) {
            buffor[i++] = b;
        }

        try {
            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            Thread.sleep(200);
            buffor = atmega.readFrom(adres, MAX_ROZMIAR_ODPOWIEDZI);
            return buffor[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void sendAnything(String msg, int adres) {
        byte[] buff = new byte[msg.length()];
        logger.debug(msg);
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) msg.charAt(i);
        }
        try {
            atmega.writeTo(adres, buff);
            logger.debug(new String(buff));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public byte[] getAnything(int adres) throws Exception {
        return atmega.readFrom(adres, 8);
    }


}
