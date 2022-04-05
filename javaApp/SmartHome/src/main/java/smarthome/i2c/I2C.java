package smarthome.i2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import smarthome.exception.HardwareException;

@Service
public class I2C{

    ArrayList<I2CDevice> devices;
    Logger logger;

    //Do restartowania
    GpioController gpio;
    GpioPinDigitalOutput pin;

    public I2C() {
        logger = LoggerFactory.getLogger(this.getClass());
        devices = new ArrayList<>();
        try {
            gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "RESET", PinState.HIGH);
            restartSlaves();
            Thread.sleep(5000);
            logger.info("Searching for devices");
            findAll();
            
        } catch (UnsatisfiedLinkError e) {
            System.err.println("platform does not support this driver");
        } catch (UnsupportedBusNumberException e) {
            System.err.println("platform does not support this driver");//TODO
        }catch (Exception e) {
            System.err.println("platform does not support this driver");

        }
    }


    public void findAll() throws UnsupportedBusNumberException, IOException{
        List<Integer> validAddresses = new ArrayList<Integer>();
        final I2CBus bus;
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            // for (int i = 1; i < 10; i++) {
            for (int i = 7; i < 128; i++) {
                try {
                    I2CDevice device = bus.getDevice(i);
                    device.write((byte) 0);
                    byte[] buffer = new byte[8];
                    device.read(buffer, 0, 8);
                    // for (byte b : buffer) {
                    //     System.out.print((char) b);
                    // }
                    System.out.println("Dodano Slave o adresie: "+i);
                    if (!devices.contains(device)) {
                        devices.add(device);
                    }
                    if (!validAddresses.contains(i)) {
                        validAddresses.add(i);
                    }
                } catch (Exception ignore) {
                    // System.out.println("Sprawdzono: "+i+" i nie jest to prawidłowy adres");
                    // ignore.printStackTrace();
                    //ignorujemy... świadczy o tym że nie ma urządzenia z takim adresem
                }
            }
        } catch (Exception e) {
            throw e;
        }
        
        logger.debug("Znaleziono Slave-ów: {}", devices);

        // System.out.println("Found: ---");
        // for (int a : validAddresses) {
        // System.out.println("Address: " + Integer.toHexString(a));
        // }
        // System.out.println("----------");
    }
    

    public void writeTo(int adres, byte[] buffer) throws HardwareException{
        I2CDevice tmp = null;
        for (I2CDevice device : devices) {
            if (device.getAddress() == adres) {
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new HardwareException("System nie znalazł urządzenia o takim adresie");
        } else {
            try {
                tmp.write(buffer);
            } catch (IOException e) {
                HardwareException throwable = new HardwareException("Błąd IO podczas próby wysyłania danych do slave-a o adresie: " + adres, e);

                logger.error(e.getLocalizedMessage(), throwable);
                
                restartSlaves();

                logger.warn("Ponowna próba wysłania komendy...");
                try {
                    tmp.write(buffer);
                } catch (Exception e2) {
                    throw new HardwareException("Błąd IO podczas próby wysyłania danych do slave-a o adresie: " + adres,e2);
                }
                logger.info("Wysłano!");
            }
        }
    }
    public void writeTo(int adres, byte[] buffer, int size) throws HardwareException{
        I2CDevice tmp = null;
        byte[] tmpbuff = new byte[size];
        for (I2CDevice device : devices) {
            if (device.getAddress() == adres) {
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new HardwareException("System nie znalazł urządzenia o takim adresie");
        } else {
            for (int i = 0; i < size; i++) {
                tmpbuff[i] = buffer[i];
            }
            try {
                tmp.write(tmpbuff);
            } catch (IOException e) {
                throw new HardwareException("Błąd IO podczas próby wysyłania danych do slave-a o adresie: "+adres, e);
            }
        }
    }
    public byte[] readFrom(int adres, int size) throws HardwareException{
        byte[] buffer = new byte[size];
        I2CDevice tmp = null;
        for (I2CDevice device : devices) {
            if(device.getAddress() == adres){
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new HardwareException("System nie znalazł urządzenia o takim adresie");
        }
        else{
            try {
                tmp.read(buffer, 0, size);
            } catch (IOException e) {
                throw new HardwareException("Błąd IO podczas próby odczytu z slave-a o adresie: "+ adres, e);
            }
        }
        return buffer;
    }
    
    /**
     * Odcina zasilanie slave-ów na krótki czas aby wymusić ich ponowne uruchomienie
     */
    public void restartSlaves() {
        logger.info("Restartowanie slave-ów");

        
        pin.setShutdownOptions(true, PinState.HIGH);//TODO czy napewno po wyłączeniu powinien być w stanie HIGH?
        pin.high();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }

        pin.low();

        try {
            Thread.sleep(5000);//oczekiwanie na uruchomienie się slave-ów
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }

        logger.info("Slave-y zrestartowane");
    }


    public List<I2CDevice> getDevices() {
        return this.devices;
    }
}
