package smarthome.i2c;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.pi4j.io.serial.Serial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Switch;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Sensor;
import smarthome.model.hardware.SensorsTypes;
import smarthome.model.hardware.Termometr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Klasa odpowiadająca za kompunikację pomiędzy Raspi a Atmegami
 * 
 * @author Marek Pałdyna
 */
@Service
public class JtAConverter {

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
    // #endregion

    @Autowired
    public I2C atmega;

    @Autowired
    SystemDAO system;

    /** Logger Springa */
    Logger logger;

    JtAConverter() {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Stworzno JtAConverter");
    }

    /**
     * Zmien stan przekaznika
     * 
     * @param przekaznik - przekaznik docelowy
     * @param stan       - stan przekaznika
     */
    public void changeSwitchState(int idPrzekaznika, int idPlytki, boolean stan) {
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_PRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) idPrzekaznika;
        buffor[i++] = (byte) (stan == true ? 1 : 0);
        try {
            atmega.writeTo(idPlytki, buffor);
            byte[] response = atmega.readFrom(idPlytki, 8);//TODO obsluga bledu
            logger.debug(Arrays.toString(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeBlindState(Blind roleta, boolean stan) {
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_ROLETY) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) roleta.getOnSlaveID();
        if (stan == true) {
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
            byte[] response = atmega.readFrom(termometr.getSlaveID(), 8);
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
    public int addUrzadzenie(Device device) {
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
                System.out.println("Writing to addres "+ device.getSlaveID());
                atmega.writeTo(device.getSlaveID(), buffor);
                Thread.sleep(100);//TODO czy jest potrzebne?
                byte[] response = atmega.readFrom(device.getSlaveID(), 8);//
                int OnBoardID = (int)response[0];
                return OnBoardID;
            } catch (Exception e) {
                e.printStackTrace();
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
                logger.debug("Writing to addres " + device.getSlaveID());
                atmega.writeTo(device.getSlaveID(), buffor);
                Thread.sleep(100);
                byte[] response = atmega.readFrom(device.getSlaveID(), 8);//TODO: dodawanie przekaźników o id podanym w odpowiedzi!
                int OnBoardID = (int) response[0];
                

                return OnBoardID;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
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
                buffor = atmega.readFrom(sens.getSlaveID(), 8);
                return buffor;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
        return null;
    }
    public boolean checkInitOfBoard(int adres){
        byte[] buffor = new byte[1];
        int i = 0;
        for (byte b : CHECK_INIT) {
            buffor[i++] = b;
        }

        try {
            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            Thread.sleep(200);
            buffor = atmega.readFrom(adres, 8);
            return buffor[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean reInitBoard(int adres) {
        byte[] buffor = new byte[1];
        int i = 0;
        for (byte b : REINIT) {
            buffor[i++] = b;
        }

        try {
            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            Thread.sleep(200);
            buffor = atmega.readFrom(adres, 8);
            return buffor[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // public void addPrzycisk(Device device) {//TODO ?
    //     if (device.getTyp() == DeviceTypes.PRZYCISK) {
    //         byte[] buffor = new byte[3];
    //         int i = 0;
    //         for (byte b : DODAJPRZYCISK) {
    //             buffor[i++] = b;
    //         }
    //         buffor[i++] = (byte) device.getPin();
    //         try {
    //             atmega.writeTo(device.getIDPlytki(), buffor);
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //     } else if (device.getTyp() == DeviceTypes.PRZEKAZNIK || device.getTyp() == DeviceTypes.SWIATLO) {
    //         addUrzadzenie(device);
    //     } else if (device.getTyp() == DeviceTypes.TERMOMETR) {
    //         addTermometr(device);
    //     }
    // }

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
