package newsmarthome.i2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SlaveNotFoundException;

@Service
public class I2CHardware implements I2C{

    /** Slave-y znalezione na magistrali (adres -> slave). Współbieżna mapa, bo findAll() modyfikuje ją w trakcie odczytów z innych wątków */
    final Map<Integer, I2CSlave> devices = new ConcurrentHashMap<>();
    Logger logger;

    //Do restartowania
    GpioController gpio;
    GpioPinDigitalOutput pin;

    /**
     * Blokada magistrali I2C. Każda transakcja (zapis + odczyt odpowiedzi) wykonywana jest pod tą blokadą,
     * dzięki czemu odpowiedzi slave-a nie mieszają się pomiędzy wątkami.
     * Blokada jest "fair" - wątek czekający najdłużej dostaje magistralę jako pierwszy, więc odpytywanie
     * eventów z przycisków czeka co najwyżej na jedną bieżącą transakcję.
     */
    private final ReentrantLock busLock = new ReentrantLock(true);

    /** Czy trwa restart slave-ów - zapobiega zapętleniu restartSlaves() -> findAll() -> restartSlaves(). Zapisywane pod busLock, czytane także bez niej */
    private volatile boolean restarting = false;

    /** Serializuje impulsy RESET - kilka wątków odzyskujących zablokowaną magistralę nie może przeplatać stanów LOW/HIGH */
    private final ReentrantLock resetLock = new ReentrantLock();

    /** Limit czasu skanowania magistrali w findAll() */
    private static final long SCAN_TIMEOUT_MS = 5_000;
    /**
     * Maksymalny czas oczekiwania findAll() na zwolnienie magistrali. Dłuższy niż najdłuższa poprawna transakcja
     * (ponowienia zapisu/odczytu + opóźnienia), więc przekroczenie oznacza, że wątek trzymający blokadę utknął w I/O.
     */
    private static final long BUS_LOCK_TIMEOUT_MS = 10_000;

    public I2CHardware() {
        logger = LoggerFactory.getLogger(this.getClass());
        try {
            gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "RESET", PinState.HIGH);
            findAll();
            logger.info("Searching for devices");

        }
        catch (UnsatisfiedLinkError e) {
            logger.error("platform does not support this driver");
        }catch (Exception e) {
            logger.error("platform does not support this driver");

        }
    }

    /**
     * Zajmuje magistralę I2C na wyłączność bieżącego wątku. Musi być zawsze sparowane z {@link #unlockBus()} w bloku finally.
     */
    public void lockBus() {
        busLock.lock();
    }

    /**
     * Zwalnia magistralę I2C zajętą przez {@link #lockBus()}
     */
    public void unlockBus() {
        busLock.unlock();
    }

    /**
     * Wykonuje atomową transakcję: wysyła komendę do slave-a, opcjonalnie czeka i odczytuje odpowiedź.
     * Żaden inny wątek nie skorzysta z magistrali pomiędzy zapisem a odczytem.
     *
     * @param address - adres slave-a
     * @param command - komenda do wysłania
     * @param delayMs - czas oczekiwania pomiędzy zapisem a odczytem (0 - bez czekania)
     * @param responseSize - rozmiar odpowiedzi
     * @return odpowiedź slave-a
     */
    public byte[] transaction(int address, byte[] command, long delayMs, int responseSize) throws HardwareException {
        busLock.lock();
        try {
            writeTo(address, command);
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // slave mógł jeszcze nie skończyć pracy (np. dodawanie czujnika, reinicjalizacja) - nie czytamy niepełnej odpowiedzi
                    throw new HardwareException("Przerwano oczekiwanie na odpowiedź slave-a o adresie: " + address, e);
                }
            }
            return readFrom(address, responseSize);
        } finally {
            busLock.unlock();
        }
    }

    public void findAll(){
        logger.debug("Szukanie Slave-ów");
        final I2CBus bus;
        if (!acquireBusOrRecover("skanowanie")) {
            return;
        }
        long time =  System.currentTimeMillis();
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            for (int i = 7; i < 128; i++) {
                try {
                    long timeFromStart = System.currentTimeMillis() - time;
                    if (timeFromStart > SCAN_TIMEOUT_MS) { // jeśli czas od rozpoczęcia szukania jest dłuższy niż 5 sekund
                        final int firstUnscanned = i;
                        devices.keySet().removeIf(address -> address >= firstUnscanned); // nie zostawiaj nieaktualnych slave-ów z niesprawdzonych adresów
                        if (restarting) { // skan po restarcie też się zawiesił - nie restartuj ponownie w pętli
                            logger.error("Sprawdzanie po restarcie trwa za długo... przerywam skanowanie magistrali");
                            return;
                        }
                        logger.error("Sprawdzanie trwa za długo... najprawdopodobniej magistrala jest zablokowana. Restartuje slave-y");
                        restartSlaves();
                        return;
                    }
                    I2CDevice device = bus.getDevice(i);
                    device.write((byte) 0);
                    byte[] buffer = new byte[8];
                    device.read(buffer, 0, 8);
                    logger.debug("Znaleziono Slave o adresie: {}",i);
                    if (devices.putIfAbsent(i, new I2CSlave(device)) == null) {
                        logger.debug("Dodano Slave o adresie: {}", i);
                    }
                } catch (Exception ignore) {
                    devices.remove(i);

                    logger.debug("Sprawdzono: {} i nie jest to prawidłowy adres", i);
                    // ignorujemy... świadczy o tym że nie ma urządzenia z takim adresem
                }
            }
        } catch (UnsupportedBusNumberException e) {
            logger.warn("You are not on a Raspberry Pi so you cannot use I2C!");
        }
        catch (IOException e) {
            logger.error("Error on searching", e);
        }
        finally {
            busLock.unlock();
        }

        logger.debug("Znaleziono Slave-ów: {}", devices.size());
    }

    /**
     * Zajmuje magistralę na potrzeby skanowania lub restartu, czekając maksymalnie {@link #BUS_LOCK_TIMEOUT_MS}.
     * Jeśli magistrala jest zajęta dłużej, wątek trzymający blokadę najpewniej utknął w I/O (slave trzyma linię SDA) -
     * wtedy resetujemy slave-y bez blokady, co przerywa zawieszoną transakcję; jej wątek sam wykona później ponowne skanowanie.
     *
     * @param operation - nazwa operacji do logów
     * @return true jeśli blokada została zajęta (należy ją zwolnić), false jeśli operację należy pominąć
     */
    private boolean acquireBusOrRecover(String operation) {
        try {
            if (busLock.tryLock(BUS_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Przerwano oczekiwanie na magistralę I2C - pomijam: {}", operation);
            return false;
        }
        if (restarting) {
            logger.error("Magistrala I2C zajęta przez restart slave-ów - pomijam: {}", operation);
        } else if (!resetLock.tryLock()) { // inny wątek już resetuje zablokowaną magistralę
            logger.error("Magistrala I2C zajęta, reset slave-ów już trwa - pomijam: {}", operation);
        } else {
            try {
                logger.error("Magistrala I2C zajęta dłużej niż {} ms - najprawdopodobniej zablokowana. Resetuję slave-y i pomijam: {}", BUS_LOCK_TIMEOUT_MS, operation);
                pulseResetPin();
            } finally {
                resetLock.unlock();
            }
        }
        return false;
    }

    /**
     * Wysyła slave'om sygnał resetu (odcina zasilanie na chwilę i czeka na ich uruchomienie)
     */
    private void pulseResetPin() {
        if (pin == null) {
            logger.warn("Brak pinu RESET - nie można zrestartować slave-ów");
            return;
        }
        resetLock.lock();
        try {
            pulseResetPinLocked();
        } finally {
            resetLock.unlock();
        }
    }

    private void pulseResetPinLocked() {
        pin.setShutdownOptions(true, PinState.HIGH);
        pin.low();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }

        pin.high();

        try {
            Thread.sleep(3000);//oczekiwanie na uruchomienie się slave-ów
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }
    }

    /**
     * Wysyła komendę bezpośrednio do slave-a (synchronicznie, pod blokadą magistrali)
     */
    public void writeTo(int adres, byte[] buffer) throws HardwareException{
        busLock.lock();
        try {
            writeMessage(new I2CMessage(adres, buffer));
            if (logger.isDebugEnabled()) {
                logger.debug("Wiadomość wysłana do Slave-a o adresie: {}", adres);
            }
        } catch (SlaveNotFoundException e) {
            logger.error("Error while sending messages", e);
            restartSlaves();
            throw new HardwareException("Błąd podczas wysyłania wiadomości do Slave-a o adresie: " + adres);
        } finally {
            busLock.unlock();
        }
    }

    /**
     * @deprecated priorytet nie ma już znaczenia - wiadomości są wysyłane synchronicznie pod blokadą magistrali.
     * Użyj {@link #writeTo(int, byte[])}
     */
    @Deprecated
    public void writeTo(int adres, byte[] buffer,int priority) throws HardwareException{
        writeTo(adres, buffer);
    }

    private void writeMessage(I2CMessage msg) throws HardwareException, SlaveNotFoundException{
        if (logger.isDebugEnabled()) {
            logger.debug("Writing {} -> '{}'", Arrays.toString(msg.getData()),msg.getAddress());
        }

        I2CSlave tmp = devices.get(msg.getAddress());
        if (tmp == null) {
            throw new SlaveNotFoundException("System nie znalazł Slave-a o takim adresie: "+ msg.getAddress());
        } else {
            busLock.lock();
            try {
                tmp.getDevice().write(msg.getData());
            } catch (IOException e) {
                try {
                    retryWrite(msg.getData(), tmp.getDevice());
                } catch (HardwareException h) {
                    this.restartSlaves();
                    throw h;
                }

            } finally {
                busLock.unlock();
            }
        }
    }
    public void writeToSized(int adres, byte[] buffer, int size) throws HardwareException, SlaveNotFoundException{
        byte[] tmpbuff = new byte[size];

        System.arraycopy(buffer, 0, tmpbuff, 0, size);//kopiowanie tablicy do nowej tablicy o odpowiednim rozmiarze

        writeMessage(new I2CMessage(adres, tmpbuff));
    }
    public byte[] readFrom(int adres, int size) throws HardwareException{
        byte[] buffer = new byte[size];
        I2CSlave tmp = devices.get(adres);
        if (tmp == null) {
            throw new HardwareException("System nie znalazł Slave-a o takim adresie: " + adres);
        }
        else{
            busLock.lock();
            try {
                tmp.getDevice().read(buffer, 0, size);
            } catch (IOException e) {
                try {
                    retryRead(tmp.getDevice(), size, buffer);

                } catch (HardwareException h) {
                    this.restartSlaves();
                    throw h;
                }
            } finally {
                busLock.unlock();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got {} <- '{}'", Arrays.toString(buffer), adres);
        }
        return buffer;
    }

    /**
     * Odcina zasilanie slave-ów na krótki czas aby wymusić ich ponowne uruchomienie
     */
    @Override
    public void restartSlaves() {
        logger.info("Restartowanie slave-ów");
        if (!acquireBusOrRecover("restart pod blokadą")) {
            return; // magistrala zablokowana - slave-y zostały już zresetowane bez blokady (albo restart właśnie trwa)
        }
        boolean wasRestarting = restarting;
        restarting = true;
        try {
            pulseResetPin();

            logger.info("Slave-y zrestartowane");
            this.findAll();
        } finally {
            restarting = wasRestarting;
            busLock.unlock();
        }
    }

    @Override
    public List<Integer> getDevices() {
        return new ArrayList<>(devices.keySet());
    }

    /**
     * Sprawdza czy slave o podanym adresie został znaleziony na magistrali
     */
    public boolean isConnected(int address) {
        return devices.containsKey(address);
    }

    protected I2CSlave findDevice(int address) throws SlaveNotFoundException{
        I2CSlave device = devices.get(address);
        if (device == null) {
            throw new SlaveNotFoundException("System nie znalazł Slave-a o takim adresie: " + address);
        }
        return device;
    }

    public void retryWrite(byte[] toWrite, I2CDevice slave) throws HardwareException{
        boolean done = false;
        for (int i = 0; i < 10 && !done; i++) {
            try{
                Thread.sleep(i*50l);
                logger.warn("Retring to write '{}' to device: {}",Arrays.toString(toWrite), slave.getAddress());
                slave.write(toWrite);
                done = true;
            } catch (IOException e) {
                logger.error("error while retry to write : {}  (IOException)", e.getMessage());
            }
            catch (InterruptedException e) {
                logger.error("Sleep error while writing: {}", e.getMessage());
            }
        }
        if (!done) {
            // restartSlaves();
            throw new HardwareException("Błąd IO podczas próby wysyłania do slave-a o adresie: " + slave.getAddress());
        }
    }
    public byte[] retryRead(I2CDevice slave, int size, byte[] buff) throws HardwareException{
        boolean done = false;
        // byte[] buff = new byte[size];
        for (int i = 0; i < 10 && !done; i++) {
            try{
                Thread.sleep(i*50l);
                logger.warn("Retring to read from device: {}", slave.getAddress());
                slave.read(buff, 0, size);
                done = true;
            } catch (IOException e) {
                logger.error("Error while retring to read: {}  (IOException)", e.getMessage());
            }
            catch (InterruptedException e) {
                logger.error("Sleep error while reading: {}", e.getMessage());
            }
        }
        if (!done) {
            throw new HardwareException("Błąd IO podczas próby odczytu z slave-a o adresie: " + slave.getAddress());
        }
        else
            return buff;
    }
    @Override
    public void write(int address, byte[] buffer, int size) throws HardwareException, SlaveNotFoundException{
        writeToSized(address, buffer, size);
    }
    @Override
    public byte[] read(int address, int size, int commandID) throws HardwareException{
        return readFrom(address, size);
    }

    public byte[] read(int address, int size) throws HardwareException{
        return readFrom(address, size);
    }
}
