package newsmarthome.i2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pi4j.io.i2c.I2CDevice;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
 * 
 * @author Marek Pałdyna
 */
@Service
@Log4j2
public abstract class  BaseConverter {

    protected static final int MAX_ROZMIAR_ODPOWIEDZI = 8;
    // #region Komendy
    /** [S,U] */
    protected static final byte[] STATUS_URZADZEN = { 'S', 'U' };
    /** [S,R] */
    protected static final byte[] STATUS_RGB = { 'S', 'R' };
    /** [W] */
    protected static final byte[] CHECK_TO_WORK = { 'W' };
    /** [I] */
    protected static final byte[] CHECK_INIT = { 'I' };
    /** [R] */
    protected static final byte[] REINIT = { 'R' };
    /** [U,S] */
    protected static final byte[] ZMIEN_STAN_PRZEKAZNIKA = { 'U', 'S' }; // + id + stan
    /** [U,B] */
    protected static final byte[] ZMIEN_STAN_ROLETY = { 'U', 'B' }; // + id + stan
    /** [T] */
    protected static final byte[] POBIERZ_TEMPERATURE = { 'T' }; // + ADRESS (8byte)
    /** [H] */
    protected static final byte[] POBIERZ_TEMPERATURE_I_WILGOTNOSC = { 'H' }; // + id
    /** [A, S] */
    protected static final byte[] DODAJ_URZADZENIE = { 'A', 'S' }; // + PIN
    /** [A, R] */
    protected static final byte[] DODAJ_ROLETE = { 'A', 'R' }; // + PIN + PIN
    /** [A, P] */
    protected static final byte[] DODAJ_PRZYCISK = { 'A', 'P' }; // + PIN
    /** [A, T] */
    protected static final byte[] DODAJ_TERMOMETR = { 'A', 'T' };
    /** [A, H] */
    protected static final byte[] DODAJ_HIGROMETR = { 'A', 'H' };
    /** [P, K, L] */
    protected static final byte[] DODAJ_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L' };
    /** [P, K, L, D] */
    protected static final byte[] USUN_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L', 'D' };
    /** [S, D] */
    protected static final byte[] SPRAWDZ_STAN_URZADZENIA = { 'S', 'D' };
    /** [C, T, N] */
    protected static final byte[] ILE_TERMOMETROW = { 'C', 'T', 'N' };
    /** [W] */
    protected static final byte[] SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA = { 'W' };
    /** [G] */
    protected static final byte[] ODBIERZ_KOMENDE = { 'G' };
    // #endregion


    /**
     * Zwraca listę adresów slave-ów które są podłączone do mastera
     */
    public abstract <T> List<T> getSlavesAdresses();

    /**
     * Sprawdza czy slave o podanym adresie jest podłączony do mastera
     * 
     * @param slaveAdress - adres slave-a do sprawdzenia
     * @return true jeśli slave o podanym adresie jest podłączony do mastera
     */
    public abstract boolean isSlaveConnected(int slaveAdress);

    /**
     * Zmien stan przekaznika
     * 
     * @param przekaznik - id przekaźnika na slavie
     * @param stan       - stan przekaznika
     */
    public abstract void changeSwitchState(int idPrzekaznika, int idPlytki, DeviceState stan) throws HardwareException;

    public abstract void changeBlindState(Blind roleta, DeviceState stan) throws HardwareException;

    /**
     * Sprawdz i zaaktualizuj temperaturę dla podanego termometra
     * 
     * @param termometr - termometr docelowy
     */
    public abstract Float checkTemperature(Termometr termometr);

    public abstract byte[] checkHighrometr(Higrometr higrometr) throws SoftwareException, HardwareException;

    /**
     * Wysyła komendę dodającą nowe urządzenia (LIGHT/GNIAZDKO)
     * 
     * @param device urządzenie do dodania
     * @return id na płytce (-1 jeśli nie powiodło się)
     */
    public abstract int addUrzadzenie(Device device) throws HardwareException;

    public abstract int[] addTermometr(int slaveAdress) throws HardwareException;

    public abstract int addHigrometr(Higrometr higrometr) throws HardwareException, SoftwareException;

    public abstract int addPrzycisk(Button button) throws HardwareException;

    /**
     * Wysyła funkcję kilknięć lokalną
     * 
     * @param function - funkcja do wysłania
     * @return
     * @throws HardwareException
     */
    public abstract int sendClickFunction(ButtonLocalFunction function) throws HardwareException;

    public abstract int sendRemoveFunction(int slaveID, int numberOfClicks) throws HardwareException;

    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany
     * 
     * @param adres - adres slave-a który zostanie zapytany
     * @return true jeśli slave był już zainicjowany
     */
    public abstract boolean checkInitOfBoard(int adres) throws SoftwareException, HardwareException;

    /**
     * Wysyła komendę do slave-a po której slave usuwa wszystkie zapisane u siebie
     * urządzenia
     * 
     * @param adres - adres slave-a na który zostanie wysłana komenda
     * @return true jeśli slave odpowie, że dostał komendę
     */
    public abstract boolean reInitBoard(int adres);

    /**
     * Sprawdza czy slave o podanym adresie był już zainicjowany i jeśli nie to go
     * inicjuje
     * 
     * @param boardAdress - adres slave-a który zostanie zapytany
     * @throws SoftwareException
     * @throws HardwareException
     * @return true jeśli slave wymagał reinicjalizacji
     */
    public abstract boolean checkAndReinitBoard(int boardAdress) throws SoftwareException, HardwareException;

    /**
     * Sprawdza stan urządzenia o podanym id na slave o podanym adresie
     * 
     * @param slaveID         - adres slave-a
     * @param onSlaveDeviceId - id urządzenia na slave-u
     * @return stan urządzenia (potrzeba mapowania na DeviceState)
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z
     *                           salve-a
     */
    public abstract int checkDeviceState(int slaveID, int onSlaveDeviceId) throws HardwareException;

    /**
     * Sprawdza ile jest dostępnych termomterów na slavie o podanym adresie
     * 
     * @param slaveAdress - adres slave-a
     * @return ile termomterów jest dostępnych na danym slavie
     * @throws HardwareException - kiedy nastąpi błąd podczas pisania do / odczytu z
     *                           salve-a
     */
    public abstract int howManyThermometersOnSlave(int slaveAdress) throws HardwareException;
}
