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
    final byte[] STATUSURZADZEN = { 'S', 'U' };
    /**[S,R]*/
    final byte[] STATUSRGB = { 'S', 'R' };
    /**[W]*/
    final byte[] CHECKTOWORK = { 'W' };
    /**[U]*/
    final byte[] ZMIENSTANPRZEKAZNIKA = { 'U' }; // + id + stan
    /**[T]*/
    final byte[] POBIERZTEMPERATURE = { 'T' }; // + ADRESS (8byte)
    /**[A, S]*/
    final byte[] DODAJURZADZENIE = { 'A', 'S' }; // + PIN
    /**[A, R]*/
    final byte[] DODAJROLETE = { 'A', 'R' }; // + PIN + PIN
    /**[A, P]*/
    final byte[] DODAJPRZYCISK = { 'A', 'P' }; // + PIN
    /**[A, T]*/
    final byte[] DODAJTERMOMETR = { 'A', 'T' };
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
    public void changeSwitchState(Switch przekaznik, boolean stan) {
        byte[] buffor = new byte[8];
        int i = 0;
        for (byte b : ZMIENSTANPRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) przekaznik.getPin();
        buffor[i++] = (byte) (stan == true ? 1 : 0);
        try {
            // atmega.writeTo(przekaznik.getIDPlytki(), buffor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public void changeBlindState(Roleta roleta, boolean stan) {
    //     // TODO
    // }

    /**
     * Sprawdz i zaaktualizuj temperaturę dla podanego termometra
     * 
     * @param termometr - termometr docelowy
     */
    public Float checkTemperature(Termometr termometr) {//TODO dostosować implementację do nowego schematu komunikacji (identyfikacja przez adress)
        byte[] buffor = new byte [9];
        String bufString = "";

        int i =0;
        for (byte b : POBIERZTEMPERATURE) {
            buffor[i++] = b;
            bufString += (int)b + " ";
        }
        for (int adr : termometr.getAddres()) {
            buffor[i++] = (byte) adr;
            bufString += adr + " ";
        }
        try {
            logger.debug("Writing to addres " + termometr.getSlaveID() + " command: " + buffor.toString());
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
            for (byte b : DODAJURZADZENIE) {
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
                byte[] response = atmega.readFrom(device.getSlaveID(), 8);
                int OnBoardID = (int)response[0];
                return OnBoardID;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(device.getTyp() == DeviceTypes.BLIND){
            byte[] buffor = new byte[4];
            int i = 0;
            for (byte b : DODAJROLETE) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) (((Blind) device).getPinUp());
            buffor[i++] = (byte) (((Blind) device).getPinDown());
            
            try {
                logger.debug("Writing to addres " + device.getSlaveID());
                atmega.writeTo(device.getSlaveID(), buffor);
                Thread.sleep(100);
                byte[] response = atmega.readFrom(device.getSlaveID(), 8);
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
            for (byte b : DODAJTERMOMETR) {
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

    // public void addPrzycisk(Device device) {
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
