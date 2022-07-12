package smarthome.i2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

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
    volatile boolean isOccupied = false;

    public I2C() {
        logger = LoggerFactory.getLogger(this.getClass());
        devices = new ArrayList<>();
        try {
            gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "RESET", PinState.HIGH);
            // restartSlaves();//TODO: zamienić na metodę restartSlaves()
            findAll();// TODO: zamienić na metodę restartSlaves()
            // logger.info("Searching for devices");
            
        } catch (UnsatisfiedLinkError e) {
            System.err.println("platform does not support this driver");
        }catch (Exception e) {
            System.err.println("platform does not support this driver");

        }
    }

    public void setOccupied(boolean isOccup){
        if (isOccup) {
            // logger.debug("Occupied");
            this.isOccupied = isOccup;
        }
        else{
            // logger.debug("END Occupied start");
            // new Thread(()->{
                try {
                    Thread.sleep(5);//opóźnienie przed kolejną operacją odczytu/wysłania
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.isOccupied = false;
                // logger.debug("END Occupied stop");
            // }).start();
            
        }
        
    }

    public void pauseIfOcupied() {
        while (isOccupied) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void findAll(){
        List<Integer> validAddresses = new ArrayList<>();
        final I2CBus bus;
        // pauseIfOcupied();
        // setOccupied(true);
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            for (int i = 7; i < 128; i++) {
                try {
                    I2CDevice device = bus.getDevice(i);
                    device.write((byte) 0);
                    byte[] buffer = new byte[8];
                    device.read(buffer, 0, 8);
                    logger.debug("Znaleziono Slave o adresie: {}",i);
                    boolean was = false;
                    for (I2CDevice dev : devices) {
                        if (dev.getAddress() == i) {
                            was = true;
                            break;
                        }
                    }
                    if (!was) {
                        devices.add(device);
                        logger.debug("Dodano Slave o adresie: {}", i);
                    }
                    if (!validAddresses.contains(i)) {
                        validAddresses.add(i);
                    }
                } catch (Exception ignore) {
                    I2CDevice tmp = null;
                    for (I2CDevice dev : devices) {
                        if (dev.getAddress() == i) {
                            tmp = dev;
                            break;
                        }
                    }
                    devices.remove(tmp);

                    // System.out.println("Sprawdzono: "+i+" i nie jest to prawidłowy adres");
                    // ignore.printStackTrace();
                    //ignorujemy... świadczy o tym że nie ma urządzenia z takim adresem
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        // setOccupied(false);

        logger.debug("Znaleziono Slave-ów: {}", devices.size());
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

                // Thread.sleep(100);
                tmp.write(buffer);
                
                
            } catch (IOException e) {
                try {
                    retryWrite(buffer, tmp);
                } catch (HardwareException h) {
                    this.restartSlaves();
                    throw h;
                }

            } 
        //     catch (InterruptedException e) {
        //        //TODO Auto-generated catch block
        //         e.printStackTrace();
        //    }
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
                //Thread.sleep(100);
                tmp.write(tmpbuff);
            } catch (IOException e) {
                try {
                    retryWrite(buffer, tmp);
                } catch (HardwareException h) {
                    this.restartSlaves();
                    throw h;
                }

            } //catch (InterruptedException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
          //  }
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

                //Thread.sleep(100);
                tmp.read(buffer, 0, size);
            } catch (IOException e) {
                try {
                    retryRead(tmp, size, buffer);
                    
                } catch (HardwareException h) {
                    this.restartSlaves();
                    throw h;
                }
            } //catch (InterruptedException e) {
                //e.printStackTrace();
          //  }
        }
        return buffer;
    }
    
    /**
     * Odcina zasilanie slave-ów na krótki czas aby wymusić ich ponowne uruchomienie
     */
    public void restartSlaves() {
        logger.info("Restartowanie slave-ów");
        pauseIfOcupied();
        setOccupied(true);
        
        pin.setShutdownOptions(true, PinState.HIGH);
        pin.low();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }
        
        pin.high();
        
        try {
            Thread.sleep(2000);//oczekiwanie na uruchomienie się slave-ów
        } catch (InterruptedException e) {
            logger.error("BŁĄD PODCZAS USYPIANIA WĄTKU", e);
        }

        setOccupied(false);

        this.findAll();
        logger.info("Slave-y zrestartowane");
    }


    public List<I2CDevice> getDevices() {
        return this.devices;
    }

    public void retryWrite(byte[] toWrite, I2CDevice slave) throws HardwareException{
        boolean done = false;
        for (int i = 0; i < 10 && !done; i++) {
            logger.warn("Retring to write '{}' to device: {}",Arrays.toString(toWrite), slave.getAddress());
            try{
                slave.write(toWrite);
                done = true;
            } catch (IOException e) {
                logger.error("error");
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
            logger.warn("Retring to read from device: {}", slave.getAddress());
            try{
                slave.read(buff, 0, size);
                done = true;
            } catch (IOException e) {
                logger.error("error");
            }
        }
        if (!done) {
            throw new HardwareException("Błąd IO podczas próby odczytu z slave-a o adresie: " + slave.getAddress());
        }
        else
            return buff;
    }
}
