package newsmarthome.model.hardware.device;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import smarthome.exception.HardwareException;

@Component
public class Outlet extends Device{
    /** [U,S] */
    final byte[] ZMIEN_STAN_PRZEKAZNIKA = { 'U', 'S' }; // + id + stan
    /** [A, S] */
    final byte[] DODAJ_URZADZENIE = { 'A', 'S' }; // + PIN


    /** Przekaźnik który odpowiada za sterowanie światłem na slavie */
    Switch swt;

    
    public Outlet(){
        super(DeviceTypes.GNIAZDKO);
        swt = new Switch();
        logger = LoggerFactory.getLogger(this.getClass());
    }
    public Outlet(int pin){
        super(DeviceTypes.GNIAZDKO);
        this.swt = new Switch(DeviceState.OFF,pin);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Outlet(DeviceState stan, int pin, int slaveID) {
        super(slaveID, DeviceTypes.GNIAZDKO);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(stan, pin);
    }

    public Outlet(int id, int room, int roomID, int pin){
        super(id, room, roomID, DeviceTypes.GNIAZDKO);
        logger = LoggerFactory.getLogger(this.getClass());
        this.swt = new Switch(DeviceState.OFF,pin);
    }    

    @Override
    public void configureToSlave() {
        byte[] buffor = new byte[3];
        int i = 0;
        for (byte b : DODAJ_URZADZENIE) {
            buffor[i++] = b;
        }
        buffor[i] = (byte) (this.getPin());

        logger.debug("Writing to addres {}", this.getSlaveID());

        try {
            i2c.write(this.getSlaveID(), buffor, 3);
            logger.debug("Reading from addres {}", this.getSlaveID());
            Thread.sleep(10);
            byte[] response = i2c.read(this.getSlaveID(), 8);
            this.setOnSlaveID(response[0]);
            logger.debug("Response from {}: {}", this.getSlaveID(), Arrays.toString(response));
        } catch (HardwareException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }
        catch (InterruptedException e1){
            logger.error(e1.getMessage(), e1);
        }
        
        
    }

    
    @Override
    public DeviceState getState() {
        return this.swt.getStan();
    }


    /**
     * This function sets the state of a device and sends a command to a slave device using I2C
     * communication protocol.
     * 
     * @param stan stan is an object of the DeviceState class, which represents the state of a device
     * (either ON or OFF). The method sets the state of a device to the specified state
     * and sends a command to a slave device via I2C communication.
     */
    public void setState(DeviceState stan) {
        this.swt.setStan(stan);
        byte[] buffor = new byte[4];
        int i = 0;
        for (byte b : ZMIEN_STAN_PRZEKAZNIKA) {
            buffor[i++] = b;
        }
        buffor[i++] = (byte) this.getOnSlaveID();
        buffor[i] = (byte) (stan == DeviceState.ON ? 1 : 0);
        try {
            i2c.writeTo(this.getSlaveID(), buffor);
            byte[] response = i2c.readFrom(this.getSlaveID(), 8);// TODO obsluga bledu
            logger.debug("Response from {}: {}", this.getSlaveID(), Arrays.toString(response));
        } catch (HardwareException e) {
            logger.error("Error on setting state! -> {}",e.getMessage());
        }

    }

    @Override
    public void changeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla gniazdka. Podany stan = " + state);
        }

        this.setState(state);
    }

    @Override
    public void changeState() {
        this.setState(this.swt.getStan().equals(DeviceState.ON) ? DeviceState.OFF : DeviceState.ON);
    }

    @Override
    public void changeToOppositeState(DeviceState state) {
        if (state != DeviceState.ON && state != DeviceState.OFF) {
            throw new IllegalArgumentException("Nie prawidłowy stan dla gniazdka. Podany stan = " + state);
        }
        
        if (this.getState() == state) {
            this.changeState();
        }
        else if (this.getState() == DeviceState.NOTKNOW) {
            if (state == DeviceState.ON) {
                this.changeState(DeviceState.OFF);
            }
            else {
                this.changeState(DeviceState.ON);
            }
        }
    }

    public void setPin(int pin){
        swt.setPin(pin);
    }
    public int getPin(){
        return swt.getPin();
    }


    public Switch getSwt() {
        return this.swt;
    }
    

    @Override
    public String toString() {
        return "{" +
            " swt=" + swt.toString() + "" +
            " super = "+ super.toString() +
            "}";
    }
    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.ON || state == DeviceState.OFF;
    }
    
    
}
