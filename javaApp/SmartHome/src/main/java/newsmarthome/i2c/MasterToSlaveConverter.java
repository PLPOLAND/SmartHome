package newsmarthome.i2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// import newsmarthome.database.SystemDAO;
import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Termometr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Klasa odpowiadająca za kompunikację pomiędzy Masterem a Slave-ami
 * @author Marek Pałdyna
 */
@Service
public class MasterToSlaveConverter {

    private static final int MAX_ROZMIAR_ODPOWIEDZI = 8;
    /** Maksymalna sensowna liczba eventów zgłoszona przez slave-a w odpowiedzi na 'W' (kolejka na slave-ie jest mała) */
    private static final int MAX_EVENTS_IN_QUEUE = 16;
    // #region Komendy
    /**[S,U]*/
    private static final byte[] STATUS_URZADZEN = { 'S', 'U' };
    /**[S,R]*/
    private static final byte[] STATUS_RGB = { 'S', 'R' };
    /**[W]*/
    private static final byte[] CHECK_TO_WORK = { 'W' };
    /**[I]*/
    private static final byte[] CHECK_INIT = { 'I' };
    /**[R]*/
    private static final byte[] REINIT = { 'R' };
    /**[U,S]*/
    private static final byte[] ZMIEN_STAN_PRZEKAZNIKA = { 'U' , 'S'}; // + id + stan
    /**[U,B] */
    private static final byte[] ZMIEN_STAN_ROLETY = { 'U' , 'B'}; // + id + stan
    /**[T]*/
    private static final byte[] POBIERZ_TEMPERATURE = { 'T' }; // + ADRESS (8byte)
    /**[H]*/
    private static final byte[] POBIERZ_TEMPERATURE_I_WILGOTNOSC = { 'H' }; // + id
    /**[A, S]*/
    private static final byte[] DODAJ_URZADZENIE = { 'A', 'S' }; // + PIN
    /**[A, R]*/
    private static final byte[] DODAJ_ROLETE = { 'A', 'R' }; // + PIN + PIN
    /**[A, P]*/
    private static final byte[] DODAJ_PRZYCISK = { 'A', 'P' }; // + PIN
    /**[A, T]*/
    private static final byte[] DODAJ_TERMOMETR = { 'A', 'T' };
    /**[A, H]*/
    private static final byte[] DODAJ_HIGROMETR = { 'A', 'H' };
    /**[P, K, L] */
    private static final byte[] DODAJ_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L' };
    /**[P, K, L, D] */
    private static final byte[] USUN_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L', 'D' };
    /**[S, D] */
    private static final byte[] SPRAWDZ_STAN_URZADZENIA = { 'S', 'D' };
    /**[C, T, N] */
    private static final byte[] ILE_TERMOMETROW = { 'C', 'T', 'N' };
    /**[W] */
    private static final byte[] SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA = {'W'};
    /**[G] */
    private static final byte[] ODBIERZ_KOMENDE = {'G'};
    // #endregion

    /** Czas oczekiwania [ms] pomiędzy wysłaniem komendy a odczytem odpowiedzi przy zwykłych komendach */
    private static final long STANDARD_DELAY = 0;
    /** Czas oczekiwania [ms] na odpowiedź dla komend wymagających dłuższej pracy slave-a (odczyt czujników) */
    private static final long SENSOR_DELAY = 10;
    /** Czas oczekiwania [ms] na dodanie termometru / higrometru na slave-ie */
    private static final long ADD_SENSOR_DELAY = 100;
    /** Czas oczekiwania [ms] na ponowne uruchomienie slave-a po reinicjalizacji */
    private static final long REINIT_DELAY = 300;

    @Autowired
    public I2CHardware atmega;

    /**
     * Czas oczekiwania [ms] pomiędzy wysłaniem komendy 'W' / 'G' a odczytem odpowiedzi.
     * Slave przygotowuje odpowiedź od razu w przerwaniu onReceive, więc wystarczy bardzo krótki czas.
     */
    @Value("${i2c.event.response-delay-ms:1}")
    long eventResponseDelayMs = 1;

    // @Autowired
    // SystemDAO system;

    /** Logger Springa */
    Logger logger;

    public MasterToSlaveConverter() {
        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Stworzno JtAConverter");
    }

    /**
     * Przeprowadza skanowanie magistrali i zapisuje adresy slave-ów do listy
     */
    public void findSlaves() {
        atmega.findAll();
    }

    public void restartAllSlaves(){
        atmega.restartSlaves();
    }


    /**
     * Zwraca listę adresów slave-ów które są podłączone do mastera
     */

    public List<Integer> getSlavesAdresses() {
        return atmega.getDevices();
    }

    /**
     * Sprawdza czy slave o podanym adresie jest podłączony do mastera
     * @param slaveAdress - adres slave-a do sprawdzenia
     * @return true jeśli slave o podanym adresie jest podłączony do mastera
     */
    public boolean isSlaveConnected(int slaveAdress) {
        return atmega.isConnected(slaveAdress);
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
        buffor[i] = (byte) (stan == DeviceState.ON ? 1 : 0);
        byte[] response = atmega.transaction(idPlytki, buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);//TODO obsluga bledu
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
                buffor[i] = 'U';
                logger.debug( "Wysyłanie komendy podniesienia Rolety");
                break;
            case DOWN:
                buffor[i] = 'D';
                logger.debug( "Wysyłanie komendy opuszczenia Rolety");
                break;
            case NOTKNOW:
                buffor[i] = 'S';
                logger.debug( "Wysyłanie komendy zatrzymania Rolety");
                break;
            default:
                break;
        }

        byte[] response = atmega.transaction(roleta.getSlaveID(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);//TODO obsluga bledu
        if (response != null) {
            logger.debug(Arrays.toString(response));

        } else {
            logger.debug("No response");
        }
    }

    /**
     * Sprawdz i zaaktualizuj temperaturę dla podanego termometra
     *
     * @param termometr - termometr docelowy
     */
    public Float checkTemperature(Termometr termometr) {
        byte[] buffor = new byte [9];

        int i =0;
        for (byte b : POBIERZ_TEMPERATURE) {
            buffor[i++] = b;
        }
        for (int adr : termometr.getAddres()) {
            buffor[i++] = (byte) adr;
        }
        try {
            byte[] response = atmega.transaction(termometr.getSlaveAdress(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
            logger.debug("Got response temperature from {}: {}", termometr.getSlaveAdress(), Arrays.toString(response));
            if (response[0] == 'E') {
                logger.error("Error in response from {}", termometr.getSlaveAdress());
                return -127.0f;
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
                // termometr.setTemperatura(temperatura);
                return temperatura;
            }
            else{
                throw new HardwareException("Got empty response from " + termometr.getSlaveAdress());
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return -128.f;
        }

    }

    public byte[] checkHighrometr(Higrometr higrometr) throws SoftwareException, HardwareException{

        byte[] buffor = new byte[2];
        int i = 0;
        for (byte b : POBIERZ_TEMPERATURE_I_WILGOTNOSC) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) higrometr.getOnSlaveID();
        byte[] response = atmega.transaction(higrometr.getSlaveAdress(), buffor, SENSOR_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        logger.debug("Got response humidity from {}: {}", higrometr.getSlaveAdress(), Arrays.toString(response));
        if (response[0] == 'E') {
            logger.error("Error in response from {}", higrometr.getSlaveAdress());
            throw new SoftwareException("Error while updating state of higrometr! Got error in response from slave: " + higrometr.getSlaveAdress());
        }
        return response;
    }

    /**
     * Wysyła komendę dodającą nowe urządzenia (LIGHT/GNIAZDKO)
     * @param device urządzenie do dodania
     * @return id na płytce (-1 jeśli nie powiodło się)
     */
    public int addUrzadzenie(Device device) throws HardwareException{
        if (device.getTyp() == DeviceTypes.LIGHT || device.getTyp() == DeviceTypes.GNIAZDKO || device.getTyp() == DeviceTypes.WENTYLATOR) {
            byte[] buffor = new byte[3];
            int i = 0;
            for (byte b : DODAJ_URZADZENIE) {
                buffor[i++] = b;
            }
            if(device.getTyp() == DeviceTypes.LIGHT){
                buffor[i++] = (byte) (((Light) device).getPin());
            }
            else if (device.getTyp() == DeviceTypes.GNIAZDKO) {
                buffor[i++] = (byte) (((Outlet) device).getPin());
            }
            else if (device.getTyp() == DeviceTypes.WENTYLATOR) {
                buffor[i++] = (byte) (((Fan) device).getPin());
            }
            logger.debug("Writing to addres {}", device.getSlaveID());
            byte[] response = atmega.transaction(device.getSlaveID(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
            return response[0];
        }
        if(device.getTyp() == DeviceTypes.BLIND){
            byte[] buffor = new byte[4];
            int i = 0;
            for (byte b : DODAJ_ROLETE) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) (((Blind) device).getPinUp());
            buffor[i++] = (byte) (((Blind) device).getPinDown());
            logger.debug("Writing to addres {}", device.getSlaveID());
            byte[] response = atmega.transaction(device.getSlaveID(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);//TODO: dodawanie przekaźników o id podanym w odpowiedzi!
            return response[0];
        }

        return -1;
    }

    public int[] addTermometr(int slaveAdress) throws HardwareException{

        byte[] buffor = new byte[2];
        int i = 0;
        for (byte b : DODAJ_TERMOMETR) {
            buffor[i++] = b;
        }
        logger.debug("addTermometr");
        // Wyślij prośbę o dodanie nowego termometru na płytce i odczytaj jego adres
        buffor = atmega.transaction(slaveAdress, buffor, ADD_SENSOR_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
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
            throw new HardwareException("Błąd podczas dodawania termometru! Próbowano dodać więcej termometrów niż jest podpiętych do Slave-a?");
        }
        return adress;
    }

    public int addHigrometr(Higrometr higrometr) throws HardwareException, SoftwareException{
        byte[] buffor = new byte[2];
        int i = 0;
        for (byte b : DODAJ_HIGROMETR) {
            buffor[i++] = b;
        }
        logger.debug("addHigrometr");
        buffor = atmega.transaction(higrometr.getSlaveAdress(), buffor, ADD_SENSOR_DELAY, MAX_ROZMIAR_ODPOWIEDZI);// Wyślij prośbę o dodanie nowego higrometru na płytce
        logger.debug("Got: {}", buffor);
        if (buffor[0] == 'E') {
            throw new HardwareException("Błąd podczas dodawania higrometru!", buffor);
        }
        else if (buffor[0] == 'O') {
            return buffor[1];
        }
        else{
            throw new SoftwareException("Błąd podczas dodawania higrometru! Nieoczekiwana odpowiedź", "O/E", buffor[0] + "");
        }
    }

    public int addPrzycisk(Button button)throws HardwareException{
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : DODAJ_PRZYCISK) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) button.getPin();

        logger.debug("Writing to addres {}", button.getSlaveAdress());
        byte[] response = atmega.transaction(button.getSlaveAdress(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        return response[0];
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
        logger.debug("Sending click function to slave {}", function.getButton().getSlaveAdress());
        byte[] response = atmega.transaction(function.getButton().getSlaveAdress(), buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        return response[0];
    }



    public int sendRemoveFunction(int slaveID, int numberOfClicks) throws HardwareException{
        byte[] buffor = new byte[5];
        int i = 0;
        for (byte b : USUN_LOKALNA_FUNKCJE_KLIKNIEC) {
            buffor[i++] = b;
        }
        buffor[i] = (byte)numberOfClicks;
        logger.debug("Writing to addres {}", slaveID);
        byte[] response = atmega.transaction(slaveID, buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        return response[0];
    }


    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany
     * @param adres - adres slave-a który zostanie zapytany
     * @return true jeśli slave był już zainicjowany
     */
    public boolean checkInitOfBoard(int adres) throws SoftwareException, HardwareException{
        // Wyślij zapytanie czy płytka była już zainicjowana
        byte[] response = atmega.transaction(adres, CHECK_INIT, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        if (response[0]!='I') {
            for (int j = 0; j < 5; j++) {
                logger.warn("Error on checking init of board {}. Response[0] != 'I' ; Response = {}", adres, Arrays.toString(response));
                logger.warn("Trying again");
                response = atmega.transaction(adres, CHECK_INIT, 1, MAX_ROZMIAR_ODPOWIEDZI);
                if (response[0]=='I') {
                    return response[1] == 1;
                }
            }
            StringBuilder str = new StringBuilder();
            str.append("Error on checking init of board ");
            str.append(adres);
            str.append(". ");
            str.append("Response[0] != 'I' ;");
            str.append("Response = ");
            str.append(Arrays.toString(response));
            throw new SoftwareException( str.toString());
        }
        return response[1] == 1;
    }
    /**
     * Wysyła komendę do slave-a po której slave usuwa wszystkie zapisane u siebie urządzenia
     * @param adres - adres slave-a na który zostanie wysłana komenda
     * @return true jeśli slave odpowie, że dostał komendę
     */
    public boolean reInitBoard(int adres) {
        try {
            // Wyślij komendę reinicjalizacji i poczekaj aż atmega się uruchomi ponownie
            byte[] response = atmega.transaction(adres, REINIT, REINIT_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
            return response[0] == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany i jeśli nie to go inicjuje
     * @param boardAdress - adres slave-a który zostanie zapytany
     * @throws SoftwareException
     * @throws HardwareException
     * @return true jeśli slave wymagał reinicjalizacji
     */
    public boolean checkAndReinitBoard(int boardAdress) throws SoftwareException, HardwareException{
        if (!checkInitOfBoard(boardAdress)) {
            reInitBoard(boardAdress);
            return true;
        }
        return false;
    }

    /**
     * Sprawdza stan urządzenia o podanym id na slave o podanym adresie
     * @param slaveID - adres slave-a
     * @param onSlaveDeviceId - id urządzenia na slave-u
     * @return stan urządzenia (potrzeba mapowania na DeviceState)
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public int checkDeviceState(int slaveID, int onSlaveDeviceId) throws HardwareException {
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : SPRAWDZ_STAN_URZADZENIA) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) onSlaveDeviceId;

        byte[] response = atmega.transaction(slaveID, buffor, STANDARD_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
        if (response == null || response[0] == 'E') {
            logger.error("Something went wrong while checking state of device ( onSlaveDeviceId:{} ). Got answare: {}", onSlaveDeviceId, Arrays.toString(response));
            throw new HardwareException("Error on checking state of device slaveID = " + slaveID, response);
        }else {
            return response[0];
        }
    }
    /**
     * Sprawdza ile jest dostępnych termomterów na slavie o podanym adresie
     * @param slaveAdress - adres slave-a
     * @return ile termomterów jest dostępnych na danym slavie
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public int howManyThermometersOnSlave(int slaveAdress) throws HardwareException{
        logger.debug("howManyThermometersOnSlave:");
        try {
            byte[] response = atmega.transaction(slaveAdress, ILE_TERMOMETROW, SENSOR_DELAY, MAX_ROZMIAR_ODPOWIEDZI);
            logger.debug("Got: {}", Arrays.toString(response));
            if (response[0] == 'E') {
                throw new HardwareException("Error on checking how many thermometers on slave: " + slaveAdress);
            } else {
                return response[0];
            }
        } catch (HardwareException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Sprawdza ile eventów (komend) czeka na slave-ie na odczytanie
     * @param slaveAdress - adres slave-a
     * @return liczba eventów w kolejce slave-a
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public int howManyCommandToRead(int slaveAdress) throws HardwareException{
        try {
            byte[] response = atmega.transaction(slaveAdress, SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA, eventResponseDelayMs, MAX_ROZMIAR_ODPOWIEDZI);
            // slave odpowiada 'E' przy błędzie - nie wolno traktować tego jako liczby eventów ('E' = 69)
            if (response[0] == 'E' || response[0] < 0 || response[0] > MAX_EVENTS_IN_QUEUE) {
                throw new HardwareException("Nieprawidłowa odpowiedź na 'W' od slave-a " + slaveAdress + ": " + Arrays.toString(response), response);
            }
            return response[0];
        } catch (HardwareException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
    /**
     * Odczytuje z slave-a pojedynczy event (komendę) z jego kolejki
     * @param slaveAdress - adres slave-a
     * @return surowa odpowiedź slave-a (8 bajtów)
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public byte[] readCommandFromSlave(int slaveAdress) throws HardwareException{
        try {
            byte[] response = atmega.transaction(slaveAdress, ODBIERZ_KOMENDE, eventResponseDelayMs, MAX_ROZMIAR_ODPOWIEDZI);
            if (logger.isDebugEnabled()) {
                logger.debug("readCommandFromSlave got: {}", Arrays.toString(response));
            }
            return response;
        } catch (HardwareException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Odczytuje wszystkie eventy oczekujące w kolejce slave-a ('W', a potem 'G' dla każdego eventu).
     * Całość wykonywana jest przy zajętej magistrali, żeby inny wątek nie odczytał eventu przeznaczonego dla nas.
     *
     * @param slaveAdress - adres slave-a
     * @return lista eventów (ramek zaczynających się od 'C'); pusta jeśli nic nie czeka
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z salve-a
     */
    public List<byte[]> readEventsFromSlave(int slaveAdress) throws HardwareException {
        List<byte[]> events = new ArrayList<>();
        atmega.lockBus();
        try {
            if (!atmega.isConnected(slaveAdress)) { // slave zniknął z magistrali (np. po findAll) - nie ma czego odczytywać
                return events;
            }
            int howMany = howManyCommandToRead(slaveAdress);
            for (int i = 0; i < howMany; i++) {
                byte[] command;
                try {
                    command = readCommandFromSlave(slaveAdress);
                } catch (HardwareException e) {
                    if (events.isEmpty()) {
                        throw e;
                    }
                    // eventy odczytane wcześniej zostały już zdjęte z kolejki slave-a - zwróć je, żeby nie przepadły
                    logger.error("Błąd podczas odczytu eventu {}/{} z slave-a {} - zwracam {} odczytanych eventów", i + 1, howMany, slaveAdress, events.size());
                    break;
                }
                if (command != null && command[0] == 'C') {
                    events.add(command);
                }
            }
        } finally {
            atmega.unlockBus();
        }
        return events;
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



}
