package smarthome.i2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pi4j.io.i2c.I2CDevice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smarthome.database.SystemDAO;
import smarthome.exception.HardwareException;
import smarthome.exception.SoftwareException;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonLocalFunction;
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
    /**[S, D] */
    final byte[] SPRAWDZ_STAN_URZADZENIA = { 'S', 'D' };
    /**[C, T, N] */
    final byte[] ILE_TERMOMETROW = { 'C', 'T', 'N' };
    /**[W] */
    final byte[] SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA = {'W'};
    /**[G] */
    final byte[] ODBIERZ_KOMENDE = {'G'};
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
     * @param przekaznik - id przekaźnika na slavie
     * @param stan       - stan przekaznika
     */
    public void changeSwitchState(int idPrzekaznika, int idPlytki, DeviceState stan) throws HardwareException {
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_PRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) idPrzekaznika;
        buffor[i++] = (byte) (stan == DeviceState.ON ? 1 : 0); 
        atmega.pauseIfOcupied();
        atmega.setOccupied(true);
        atmega.writeTo(idPlytki, buffor);
        byte[] response = atmega.readFrom(idPlytki, 8);//TODO obsluga bledu
        atmega.setOccupied(false);
        logger.debug("Response from {}: {}" ,idPlytki, Arrays.toString(response));
    }

    public void changeBlindState(Blind roleta, DeviceState stan) throws HardwareException{
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_ROLETY) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) roleta.getOnSlaveID();

        switch (stan) {
            case UP:
                buffor[i++] = 'U';
                logger.debug( "Wysyłanie komendy podniesienia Rolety");
                break;
            case DOWN:
                buffor[i++] = 'D';
                logger.debug( "Wysyłanie komendy opuszczenia Rolety");
                break;
            case NOTKNOW://TODO wymyślić co zrobić z tym ( nie może być NOTKNOW)
                buffor[i++] = 'S';
                logger.debug( "Wysyłanie komendy zatrzymania Rolety");
                break;
            default:
                break;
        }

        try {

            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            atmega.writeTo(roleta.getSlaveID(), buffor);
            byte[] response = atmega.readFrom(roleta.getSlaveID(), 8);//TODO obsluga bledu
            atmega.setOccupied(false);
            if (response != null) {
                logger.debug(Arrays.toString(response));
                
            } else {
                logger.debug("No response");
            }
        } catch (HardwareException e) {
            atmega.setOccupied(false);
            throw e;
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
            logger.debug("Writing to addres {} command: '{}'", termometr.getSlaveAdress(), bufString);
            
            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            atmega.writeTo(termometr.getSlaveAdress(), buffor);
            // Thread.sleep(10);
            byte[] response = atmega.readFrom(termometr.getSlaveAdress(), MAX_ROZMIAR_ODPOWIEDZI);
            atmega.setOccupied(false);
            logger.debug("Got response tempetrture from {}: {}", termometr.getSlaveAdress(), Arrays.toString(response));
            if (response[0] == 'E') {
                logger.error("Error in response from {}", termometr.getSlaveAdress());
                return null;
            }
            String tmp ="";
            for (byte b : response) {
                if (b >= 48 && b<= 57 || b == '.') {
                    tmp += (char) b;
                }
            }
            if (!tmp.equals("")) {
                Float temperatura = Float.parseFloat(tmp);
                logger.debug("Got temperature from {}. Temperature = {} *C",Arrays.toString(termometr.getAddres()),temperatura);
                termometr.setTemperatura(temperatura);
                return temperatura;
            }
            else{
                throw new HardwareException("Got empty response from " + termometr.getSlaveAdress());
            }
            
        } catch (Exception e) {
            atmega.setOccupied(false);
            logger.error(e.getMessage());
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
                atmega.pauseIfOcupied();
                atmega.setOccupied(true);
                // try {
                    logger.debug("Writing to addres {}", device.getSlaveID());
    
                    atmega.writeTo(device.getSlaveID(), buffor);
                    // Thread.sleep(10);//TODO czy jest potrzebne?
                    logger.debug("Reading from addres {}", device.getSlaveID());
                    byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                    atmega.setOccupied(false);
                    return response[0];
                // } 
                // catch (InterruptedException e) {
                //     logger.error(e.getMessage(), e);
                //     logger.debug("Próba kontynuacji");
                //     logger.debug("Reading from addres {}", device.getSlaveID());
                //     byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                //     atmega.setOccupied(false);
                //     return response[0];
                // }
                
            } catch (HardwareException e) {
                atmega.setOccupied(false);
                throw e;
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
                atmega.pauseIfOcupied();
                atmega.setOccupied(true);
                // try {
                    logger.debug("Writing to addres {}", device.getSlaveID());
                    atmega.writeTo(device.getSlaveID(), buffor);
                    // Thread.sleep(10);
                    byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//TODO: dodawanie przekaźników o id podanym w odpowiedzi!
                    atmega.setOccupied(false);
                    return response[0];
                // } 
                // catch (InterruptedException e) {
                //     logger.error(e.getMessage(), e);
                //     logger.debug("Próba kontynuacji");
                //     logger.debug("Reading from addres {}", device.getSlaveID());
                //     byte[] response = atmega.readFrom(device.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
                //     atmega.setOccupied(false);
                //     return response[0];
                // }
            }
            catch(HardwareException e){
                atmega.setOccupied(false);
                throw e;
            }
        }

        return -1;
    }

    public int[] addTermometr(int slaveAdress) throws HardwareException{ 
        
        byte[] buffor = new byte[2];
        int i = 0;
        for (byte b : DODAJ_TERMOMETR) {
            buffor[i++] = b;
        }
        try {
            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            logger.debug("addTermometr");
            try {
                atmega.writeTo(slaveAdress, buffor);// Wyślij prośbę o dodanie nowego termometru na płytce
                Thread.sleep(100);
                buffor = atmega.readFrom(slaveAdress, MAX_ROZMIAR_ODPOWIEDZI);
                logger.debug("Got: {}", buffor);
                int[] adress = new int[8];
                for (int j = 0; j < 8; j++) {
                    adress[j] = buffor[j] & 0xFF;
                }
                boolean isOnlyZeros = true;
                for (int j : buffor) {
                    if (j!=0) {
                        isOnlyZeros = false;
                    }
                }
                if (isOnlyZeros) {
                    atmega.setOccupied(false);
                    throw new HardwareException("Błąd podczas dodawania termometru! Próbowano dodać więcej termometrów niż jest podpiętych do Slave-a?");
                }
                atmega.setOccupied(false);
                return adress;
            } catch (InterruptedException e) {

                buffor = atmega.readFrom(slaveAdress, MAX_ROZMIAR_ODPOWIEDZI);
                int[] adress = new int[8];
                for (int j = 0; j < 8; j++) {
                    adress[j] = buffor[j] & 0xFF;
                }
                atmega.setOccupied(false);
                return adress;
            }
        } catch (HardwareException e) {
            atmega.setOccupied(false);
            throw e;
        }
    }

    public int addPrzycisk(Button button)throws HardwareException{
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : DODAJ_PRZYCISK) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) button.getPin();
        
        try {
                atmega.pauseIfOcupied();
                atmega.setOccupied(true);

            // try {
                logger.debug("Writing to addres {}", button.getSlaveAdress());
                atmega.writeTo(button.getSlaveAdress(), buffor);
                // Thread.sleep(10);// TODO czy jest potrzebne?
                logger.debug("Reading from addres {}", button.getSlaveAdress());
                byte[] response = atmega.readFrom(button.getSlaveAdress(), MAX_ROZMIAR_ODPOWIEDZI);//
                atmega.setOccupied(false);
                return response[0];
            // } catch (InterruptedException e) {
            //     logger.error(e.getMessage(), e);
            //     logger.debug("Próba kontynuacji");
            //     logger.debug("Reading from addres {}", button.getSlaveID());
            //     byte[] response = atmega.readFrom(button.getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            //     atmega.setOccupied(false);
            //     return response[0];
            // }
        }
        catch(HardwareException e){
            atmega.setOccupied(false);
            throw e;
        }
    }
    /**
     * Wysyła funkcję kilknięć lokalną
     * @param function - funkcja do wysłania
     * @return
     * @throws HardwareException
     */
    public int sendClickFunction(ButtonLocalFunction function) throws HardwareException{
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
            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            // try {
                logger.debug("Writing to addres {}", function.getButton().getSlaveAdress());
                atmega.writeTo(function.getButton().getSlaveAdress(), buffor);
                // Thread.sleep(10);// TODO czy jest potrzebne?
                logger.debug("Reading from addres {}", function.getButton().getSlaveAdress());
                byte[] response = atmega.readFrom(function.getButton().getSlaveAdress(), MAX_ROZMIAR_ODPOWIEDZI);//
                atmega.setOccupied(false);
                return response[0];
            // } catch (InterruptedException e) {
            //     logger.error(e.getMessage(), e);
            //     logger.debug("Próba kontynuacji");
            //     logger.debug("Reading from addres {}", function.getButton().getSlaveID());
            //     byte[] response = atmega.readFrom(function.getButton().getSlaveID(), MAX_ROZMIAR_ODPOWIEDZI);//
            //     atmega.setOccupied(false);
            //     return response[0];
            // }
        }
        catch(HardwareException e){
            atmega.setOccupied(false);
            throw e;
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
                atmega.pauseIfOcupied();
                atmega.setOccupied(true);
            // try {
                logger.debug("Writing to addres {}", slaveID);
                atmega.writeTo(slaveID, buffor);
                // Thread.sleep(10);// TODO czy jest potrzebne?
                logger.debug("Reading from addres {}", slaveID);
                byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
                atmega.setOccupied(false);
                return response[0];
            // } catch (InterruptedException e) {
            //     logger.error(e.getMessage(), e);
            //     logger.debug("Próba kontynuacji");
            //     logger.debug("Reading from addres {}", slaveID);
            //     byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
            //     atmega.setOccupied(false);
            //     return response[0];
            // }
        }
        catch(HardwareException e){
            atmega.setOccupied(false);
            throw e;
        }

    }


    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany
     * @param adres - adres slave-a który zostanie zapytany
     * @return stan zainicjowania slave-a
     */
    public boolean checkInitOfBoard(int adres) throws SoftwareException, HardwareException{
        byte[] buffor = new byte[1];
        int i = 0;
        for (byte b : CHECK_INIT) {
            buffor[i++] = b;
        }

        try {
            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            // Thread.sleep(10);
            buffor = atmega.readFrom(adres, MAX_ROZMIAR_ODPOWIEDZI);
            atmega.setOccupied(false);
            if (buffor[0]=='E') {
                // logger.error("Error on checking init of board {}", adres);
                throw new SoftwareException("Error on checking init of board " + adres);
            }
            return buffor[0] == 1;
        } catch (Exception e) {
            atmega.setOccupied(false);
            throw e;
        }
        // atmega.setOccupied(false);
        // return false;
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
            atmega.pauseIfOcupied();
            atmega.setOccupied(true);

            atmega.writeTo(adres, buffor);// Wyślij zapytanie czy płytka była już zainicjowana
            Thread.sleep(300);// Poczekaj aż atmega się uruchomi ponownie
            buffor = atmega.readFrom(adres, MAX_ROZMIAR_ODPOWIEDZI);
            atmega.setOccupied(false);
            return buffor[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
            atmega.setOccupied(false);
        }
        atmega.setOccupied(false);
        return false;
    }
    
    public int checkDeviceState(int slaveID, int onSlaveDeviceId) throws HardwareException {
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : SPRAWDZ_STAN_URZADZENIA) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) onSlaveDeviceId;

        try {

            atmega.pauseIfOcupied();
            atmega.setOccupied(true);
            // logger.debug("Writing to addres {} {}", slaveID, buffor);
            atmega.writeTo(slaveID, buffor);
            // Thread.sleep(10);// TODO czy jest potrzebne?
            // logger.debug("Reading from addres {}", slaveID);
            byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
            atmega.setOccupied(false);
            if (response[0] == 'E') {
                // logger.error("Error on checking init of board {}", slaveID);
                throw new HardwareException("Error on checking state of device slaveID = " + slaveID);
            }else {
                
            }
            return response[0];
        // } catch (InterruptedException e) {
        //     logger.error(e.getMessage());
        //     // logger.debug("Próba kontynuacji");
        //     // logger.debug("Reading from addres {}", slaveID);
        //     byte[] response = atmega.readFrom(slaveID, MAX_ROZMIAR_ODPOWIEDZI);//
        //     atmega.setOccupied(false);
        //     return response[0];
        }
        catch (HardwareException e){
            atmega.setOccupied(false);
            throw e;
        }

    }
    /**
     * Sprawdza ile jest dostępnych termomterów na slavie o podanym adresie
     * @param slaveAdress - adres slave-a
     * @return ile termomterów jest dostępnych na danym slavie
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public int howManyThermometersOnSlave(int slaveAdress) throws HardwareException{
        int ile = -1;
        logger.debug("howManyThermometersOnSlave:");
        atmega.pauseIfOcupied();
        atmega.setOccupied(true);

        atmega.writeTo(slaveAdress, ILE_TERMOMETROW);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] response;
        try {
            response = atmega.readFrom(slaveAdress, MAX_ROZMIAR_ODPOWIEDZI);
            logger.debug("Got: {}", Arrays.toString(response));
            atmega.setOccupied(false);
            if (response[0] == 'E') {
                throw new HardwareException("Error on checking how many thermometers on slave: " + slaveAdress);
            } else {
                ile = response[0];
                return ile;
            }
        } catch (HardwareException e) {
            atmega.setOccupied(false);
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Reads // TODO
     * @param slaveAdress
     * @return
     * @throws HardwareException
     */
    public int howManyCommandToRead(int slaveAdress) throws HardwareException{
        int ile = -1;
        // logger.debug("howManyCommandToRead:");
        atmega.pauseIfOcupied();
        atmega.setOccupied(true);

        byte[] response;
        try {
            atmega.writeTo(slaveAdress, SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = atmega.readFrom(slaveAdress, MAX_ROZMIAR_ODPOWIEDZI);
            // logger.debug("Got: {}", Arrays.toString(response));
            atmega.setOccupied(false);
            ile = response[0];
            return ile;
        } catch (HardwareException e) {
            atmega.setOccupied(false);
            logger.error(e.getMessage());
            throw e;
        }
    }
    /**
     * //TODO
     * @param slaveAdress
     * @return
     * @throws HardwareException
     */
    public byte[] readCommandFromSlave(int slaveAdress) throws HardwareException{
        // int ile = -1;
        logger.debug("readCommandFromSlave:");
        atmega.pauseIfOcupied();
        atmega.setOccupied(true);

        byte[] response;
        try {
            atmega.writeTo(slaveAdress, ODBIERZ_KOMENDE);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = atmega.readFrom(slaveAdress, MAX_ROZMIAR_ODPOWIEDZI);
            logger.debug("Got: {}", Arrays.toString(response));
            atmega.setOccupied(false);
            return response;
        } catch (HardwareException e) {
            atmega.setOccupied(false);
            logger.error(e.getMessage());
            throw e;
        }
    }

    
    /**
     * Only for test
     * 
     * @deprecated
     * @param msg
     * @param adres
     * @return
     */
    @Deprecated
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
    /**
     * Only for test 
     * @deprecated
     * @param adres
     * @return
     * @throws HardwareException
     */
    @Deprecated
    public byte[] getAnything(int adres) throws HardwareException {
        return atmega.readFrom(adres, 8);
    }

    public List<Integer> getSlavesAdresses() {
        ArrayList<Integer> adresy = new ArrayList<>();

        for (I2CDevice device : atmega.getDevices()) {
            adresy.add(device.getAddress());
        }

        return adresy;
    }

    public boolean isDeviceConnected(int deviceId) {
        boolean isConnected = false;

        for (I2CDevice device : atmega.getDevices()) {
            if (device.getAddress() == deviceId) {
                isConnected = true;
                break;
            }
        }

        return isConnected;
    }


}
