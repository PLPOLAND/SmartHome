package smarthome.i2c;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.pi4j.io.serial.Serial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.model.Device;
import smarthome.model.DeviceTypes;
import smarthome.model.Przekaznik;
import smarthome.model.Roleta;
import smarthome.model.Termometr;

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
    final byte[] STATUSURZADZEN = { 'S', 'U' };
    final byte[] STATUSRGB = { 'S', 'R' };
    final byte[] CHECKTOWORK = { 'W' };
    final byte[] ZMIENSTANPRZEKAZNIKA = { 'U' }; // + id + stan
    final byte[] POBIERZTEMPERATURE = { 'T' }; // + Id
    final byte[] DODAJURZADZENIE = { 'A', 'U' }; // + PIN
    final byte[] DODAJROLETE = { 'A', 'R' }; // + PIN + PIN
    final byte[] DODAJPRZYCISK = { 'A', 'P' }; // + PIN
    final byte[] DODAJTERMOMETR = { 'A', 'T' }; // + NUMERTERMOMETRA
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
    public void changeSwitchState(Przekaznik przekaznik, boolean stan) {
        byte[] buffor = new byte[8];
        int i = 0;
        for (byte b : ZMIENSTANPRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) przekaznik.getPin();
        buffor[i++] = (byte) (stan == true ? 1 : 0);
        try {
            atmega.writeTo(przekaznik.getIDPlytki(), buffor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeBlindState(Roleta roleta, boolean stan) {
        // TODO
    }

    /**
     * Sprawdz i zaaktualizuj temperaturę dla podanego termometra
     * 
     * @param termometr - termometr docelowy
     */
    public void checkTemperature(Termometr termometr) {
        byte[] buffor = new byte[8];
        int i = 0;
        for (byte b : POBIERZTEMPERATURE) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) termometr.getNumberOnBoard();
        // System.out.println(new String(buffor));
        logger.debug(buffor.toString());
        try {
            atmega.writeTo(termometr.getIDPlytki(), buffor,2);
            buffor = atmega.readFrom(termometr.getIDPlytki(), 8);// odpowiedz z temperaturą
            String bString = "";
            for (byte b : buffor) {
                if (b >= 48 && b <= 57 || b == '.') {
                    bString += (char) b;
                    // System.out.println((char)b);
                }
            }
            logger.debug(bString);
            String tmp ="";
            for (int j = 0; j < buffor.length; j++) {
                tmp+=(int)buffor[j];
            }
            logger.debug(tmp);
            if (buffor[0] == termometr.getNumberOnBoard()) {
                float tempVal = Float.parseFloat(bString);
                logger.info("Odebrano temperaturę \"" + tempVal + "\" z urządzenia" + termometr.toString());
                termometr.setTemperatura(tempVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUrzadzenie(Device device) {
        if (device.getTyp() == DeviceTypes.GNIAZDKO || device.getTyp() == DeviceTypes.SWIATLO) {
            byte[] buffor = new byte[3];
            int i = 0;
            for (byte b : DODAJURZADZENIE) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) device.getPin();

            try {
                atmega.writeTo(device.getIDPlytki(), buffor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (device.getTyp() == DeviceTypes.TERMOMETR) {
            addTermometr(device);
        } else if (device.getTyp() == DeviceTypes.PRZYCISK) {
            addPrzycisk(device);
        }
    }

    public void addTermometr(Device device) {
        if (device.getTyp() == DeviceTypes.TERMOMETR) {
            byte[] buffor = new byte[2];
            int i = 0;
            for (byte b : DODAJTERMOMETR) {
                buffor[i++] = b;
            }
            // buffor[i++] = (byte) ((Termometr) device).getNumberOnBoard();
            try {
                atmega.writeTo(device.getIDPlytki(), buffor);//Wyślij prośbę o dodanie nowego termometru na płytce
                buffor = atmega.readFrom(device.getIDPlytki(), 1);
                ((Termometr) device).setNumberOnBoard(buffor[0]);//ustaw id termometra na płytce na podstawie odpowiedzi od płytki
                logger.debug(new String(buffor));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (device.getTyp() == DeviceTypes.GNIAZDKO || device.getTyp() == DeviceTypes.SWIATLO) {
            addUrzadzenie(device);
        } else if (device.getTyp() == DeviceTypes.PRZYCISK) {
            addPrzycisk(device);
        }
    }

    public void addPrzycisk(Device device) {
        if (device.getTyp() == DeviceTypes.PRZYCISK) {
            byte[] buffor = new byte[3];
            int i = 0;
            for (byte b : DODAJPRZYCISK) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) device.getPin();
            try {
                atmega.writeTo(device.getIDPlytki(), buffor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (device.getTyp() == DeviceTypes.GNIAZDKO || device.getTyp() == DeviceTypes.SWIATLO) {
            addUrzadzenie(device);
        } else if (device.getTyp() == DeviceTypes.TERMOMETR) {
            addTermometr(device);
        }
    }

    public void sentAnything(String msg, int adres) {
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
