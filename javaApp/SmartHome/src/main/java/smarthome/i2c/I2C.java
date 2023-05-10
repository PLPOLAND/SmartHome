package smarthome.i2c;

import java.util.List;

/**
 * I2C
 */
public interface I2C {

    public void write(int address, byte[] buffer, int size) throws Exception;
    public byte[] read(int address, int size, int commandID) throws Exception;
    public void restartSlaves();
    public List<Integer> getDevices();

}