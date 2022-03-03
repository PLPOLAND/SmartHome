package smarthome.i2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import org.springframework.stereotype.Service;

@Service
public class I2C{

    ArrayList<I2CDevice> devices;


    public I2C() {
        devices = new ArrayList<>();
        try {
            findAll();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("platform does not support this driver");
        } catch (UnsupportedBusNumberException e) {
            System.err.println("platform does not support this driver");//TODO
        }catch (Exception e) {
            System.err.println("platform does not support this driver");

        }
    }


    public void findAll() throws UnsupportedBusNumberException{
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
                    devices.add(device);
                    validAddresses.add(i);
                } catch (Exception ignore) {
                    // System.out.println("Sprawdzono: "+i+" i nie jest to prawidłowy adres");
                    // ignore.printStackTrace();
                    //ignorujemy... świadczy o tym że nie ma urządzenia z takim adresem
                }
            }
        } catch (UnsupportedBusNumberException e) {
            throw e;
        }catch (Exception e) {
            e.printStackTrace();
        }
        

        System.out.println("Found: ---");
        for (int a : validAddresses) {
        System.out.println("Address: " + Integer.toHexString(a));
        }
        System.out.println("----------");
    }
    

    public void writeTo(int adres, byte[] buffer) throws Exception{
        I2CDevice tmp = null;
        for (I2CDevice device : devices) {
            if (device.getAddress() == adres) {
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new Exception("System nie znalazł urządzenia o takim adresie");
        } else {
            tmp.write(buffer);
        }
    }
    public void writeTo(int adres, byte[] buffer, int size) throws Exception{
        I2CDevice tmp = null;
        byte[] tmpbuff = new byte[size];
        for (I2CDevice device : devices) {
            if (device.getAddress() == adres) {
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new Exception("System nie znalazł urządzenia o takim adresie");
        } else {
            for (int i = 0; i < size; i++) {
                tmpbuff[i] = buffer[i];
            }
            tmp.write(tmpbuff);
        }
    }
    public byte[] readFrom(int adres, int size) throws Exception{
        byte[] buffer = new byte[size];
        I2CDevice tmp = null;
        for (I2CDevice device : devices) {
            if(device.getAddress() == adres){
                tmp = device;
            }
        }
        if (tmp == null) {
            throw new Exception("System nie znalazł urządzenia o takim adresie");
        }
        else{
            tmp.read(buffer, 0, size);
        }
        return buffer;
    }
    public ArrayList<I2CDevice> getDevices() {
        return this.devices;
    }
}
