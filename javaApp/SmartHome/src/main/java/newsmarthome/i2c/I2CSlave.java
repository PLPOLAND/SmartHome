package newsmarthome.i2c;

import com.pi4j.io.i2c.I2CDevice;

import lombok.Data;

@Data
public class I2CSlave {
    I2CDevice device;
    boolean isInitialized = false;

    public I2CSlave(I2CDevice device){
        this.device = device;
    }

}
