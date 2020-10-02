package smarthome.i2c;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class i2c {

    public void findall() throws UnsupportedBusNumberException, IOException {
        List<Integer> validAddresses = new ArrayList<Integer>();
        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        for (int i = 1; i < 128; i++) {
        try {
        I2CDevice device = bus.getDevice(i);
        device.write((byte) 0);
        byte[] buffer = new byte[8];
        device.read(buffer, 0, 8);
        for (byte b : buffer) {
        System.out.print((char)b);
        }
        validAddresses.add(i);
        } catch (Exception ignore) {
        }
        }

        System.out.println("Found: ---");
        for (int a : validAddresses) {
        System.out.println("Address: " + Integer.toHexString(a));
        }
        System.out.println("----------");
    }
    
}
