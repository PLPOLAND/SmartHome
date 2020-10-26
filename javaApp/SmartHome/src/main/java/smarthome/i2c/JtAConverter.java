package smarthome.i2c;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.model.Przekaznik;
import smarthome.model.Termometr;

/**
 * 
 * Klasa odpowiadająca za kompunikację pomiędzy Raspi a Atmegami
 * 
 * @author Marek Pałdyna
 */
@Service
public class JtAConverter {

    //#region Komendy
    final byte[] STATUSURZADZEN = {'S','U'};
    final byte[] STATUSRGB = {'S', 'R'};
    final byte[] CHECKTOWORK = {'W'};
    final byte[] ZMIENSTANPRZEKAZNIKA = {'U'}; // + id + stan
    final byte[] POBIERZTEMPERATURE = {'T'}; // + Id
    final byte[] DODAJURZADZENIE = {'A'}; // + TYP + PIN    
    //#endregion


    @Autowired
    public I2C atmega;

    @Autowired
    SystemDAO system;

    public void changeSwitchState(Przekaznik przekaznik, boolean stan) {
        byte[] buffor = new byte[8];
        int i = 0;
        for (byte b : ZMIENSTANPRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte)przekaznik.getPin();
        buffor[i++] = (byte)(stan == true ? 1:0);
        try {
            atmega.writeTo(8, buffor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkTemperature(Termometr termometr){
        byte[] buffor = new byte[8];
        int i = 0;
        for (byte b : POBIERZTEMPERATURE) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) termometr.getPin();
        try {
            atmega.writeTo(termometr.getIDPlytki(), buffor);
            buffor = atmega.readFrom(termometr.getIDPlytki(), 8);//odpowiedz z temperaturą
            System.out.println(Arrays.toString(buffor));
            System.out.println(Arrays.toString(ByteBuffer.allocate(8).putDouble(9.1).array()));
            if (buffor[0]!= termometr.getId()) {
                double tempVal = ByteBuffer.wrap(buffor).getDouble();
                System.out.println(tempVal);
                termometr.setTemperatura(tempVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
