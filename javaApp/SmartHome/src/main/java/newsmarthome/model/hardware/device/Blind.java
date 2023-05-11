package newsmarthome.model.hardware.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

import smarthome.exception.HardwareException;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Blind extends Device{

    /** [A, R] */
    final byte[] DODAJ_ROLETE = { 'A', 'R' }; // + PIN + PIN
    /**[U,B] */
    final byte[] ZMIEN_STAN_ROLETY = { 'U', 'B' }; // + id + stan

    DeviceState stan;
    Switch swtUp;
    Switch swtDown;

    public Blind(){
        super(DeviceTypes.BLIND);
        logger = LoggerFactory.getLogger(Blind.class);
    }
    
    public Blind(boolean stan, int boardID, int pinUp, int pinDown) {
        super(boardID, DeviceTypes.BLIND);
        this.stan = stan? DeviceState.UP : DeviceState.DOWN;
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    public Blind(int id, int room, int boardID, int pinUp, int pinDown){
        super(id, room, boardID, DeviceTypes.BLIND);
        stan = DeviceState.NOTKNOW;
        swtUp = new Switch(DeviceState.OFF, pinUp);
        swtDown = new Switch(DeviceState.OFF, pinDown);
        logger = LoggerFactory.getLogger(Blind.class);
    }

    @Override
    public void configureToSlave(){
        byte[] buffor = new byte[4];
            int i = 0;
            for (byte b : DODAJ_ROLETE) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) (this.getPinUp());
            buffor[i] = (byte) (this.getPinDown());
            try {
                // try {
                    logger.debug("Writing to addres {}", this.getSlaveID());
                    i2c.write(this.getSlaveID(), buffor,4);
                    Thread.sleep(10);
                    byte[] response = i2c.read(this.getSlaveID(), 8);//TODO: dodawanie przekaźników o id podanym w odpowiedzi!
                    logger.debug("Read from addres {}: {}", this.getSlaveID(), Arrays.toString(response));
                    this.setOnSlaveID(response[0]);
                }
            catch (InterruptedException e) {
                logger.error("Błąd podczas oczekiwania na zwolnienie magistrali I2C");
                e.printStackTrace();
            }
            catch (HardwareException e) {
                logger.error("Błąd podczas komunikacji z Slave'em");
                e.printStackTrace();
            }
    }

    @Override
    public void changeState(DeviceState stan){
        if (stan != DeviceState.UP && stan != DeviceState.DOWN && stan != DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = UP, DOWN lub NOTKNOW");
        }

        if(this.stan != stan){
            switch (stan) {
                case DOWN:
                    logger.debug("Zmieniam stan na: DOWN");
                    swtDown.setStan(DeviceState.ON);
                    swtUp.setStan(DeviceState.OFF);
                    this.stan = DeviceState.DOWN;
                    break;
                case UP:
                    logger.debug("Zmieniam stan na: UP");
                    swtDown.setStan(DeviceState.OFF);
                    swtUp.setStan(DeviceState.ON);
                    this.stan = DeviceState.UP;
                    break;
                case NOTKNOW://TODO Co w tedy?
                    this.stan = DeviceState.NOTKNOW;
                    break;
                default:
                    break;
            }

            //Wysyłanie komendy do slave'a
            byte[] buffor = new byte[4];
            int i = 0;
            for (byte b : ZMIEN_STAN_ROLETY) {
                buffor[i++] = b;
            }
            buffor[i++] = (byte) this.getOnSlaveID();

            switch (stan) {
                case UP:
                    buffor[i] = 'U';
                    logger.debug("Wysyłanie komendy podniesienia Rolety");
                    break;
                case DOWN:
                    buffor[i] = 'D';
                    logger.debug("Wysyłanie komendy opuszczenia Rolety");
                    break;
                case NOTKNOW:// TODO wymyślić co zrobić z tym ( nie może być NOTKNOW)
                    buffor[i] = 'S';
                    logger.debug("Wysyłanie komendy zatrzymania Rolety");
                    break;
                default:
                    break;
            }
            try {
                i2c.write(this.getSlaveID(), buffor, 4);
                byte[] response = i2c.read(this.getSlaveID(), 8);
                logger.debug("Odpowiedź od slave'a: {}", Arrays.toString(response));
                if (response == null || response[0] == 'E') {
                    throw new HardwareException("Error on changing state of device slaveID = " + this.getSlaveID());
                }
            } catch (HardwareException e) {
                logger.error("Błąd podczas komunikacji z Slave'em! -> {}", e.getMessage());
                
            }
        }
    }

    @Override
    public void changeState(){
        if(this.stan == DeviceState.DOWN){
            this.changeState(DeviceState.UP);
        }else if (this.stan == DeviceState.UP){
            this.changeState(DeviceState.DOWN);
        }
        else if(this.stan == DeviceState.NOTKNOW){
            logger.debug("Jest stan NOTKNOW więc nic nie robię");
        }
    }

    @Override
    public void changeToOppositeState( DeviceState stan){
        if (stan == DeviceState.NOTKNOW) {
            throw new IllegalArgumentException("Nie ma stanu przeciwnego dla 'NOTKNOW'; oczekiwano stanu 'UP' lub 'DOWN'");
        }
        else if (stan!=DeviceState.UP && stan!=DeviceState.DOWN) {
            throw new IllegalArgumentException("Nieprawidłowy stan dla Rolety. Podany stan = " + stan + ". Oczekiwany stan = 'UP' lub 'DOWN'");
        }
        if(this.stan == stan){
            this.changeState();
        }
        else if (this.stan == DeviceState.NOTKNOW){
            if (stan == DeviceState.DOWN) {
                this.changeState(DeviceState.UP);
            } else {
                this.changeState(DeviceState.DOWN);
            }
        }
    }

    @JsonIgnore
    public int getPinUp(){
        return swtUp.getPin();
    }
    
    @JsonIgnore
    public int getPinDown(){
        return swtDown.getPin();
    }
    @JsonIgnore
    public void setPinUp(int pin){
        swtUp.setPin(pin);
    }
    
    @JsonIgnore
    public void setPinDown(int pin){
        swtDown.setPin(pin);
    }
    public Switch getSwitchUp() {
        return swtUp;
    }
    public Switch getSwitchDown() {
        return swtDown;
    }
    public void setSwitchUp(Switch swt) {
        swtUp = swt;
    }
    public void setSwitchDown(Switch swt) {
        swtDown = swt;
    }


    @Override
    public DeviceState getState(){
        if (this.stan == null) {
            this.changeState(DeviceState.NOTKNOW);
        }
        return this.stan;
    }

    @Override
    public String toString() {
        return "{" +
            " stan='" + getState() + "'" +
            ", swtUp='" + swtUp.toString() + "'" +
            ", swtDown='" + swtDown.toString() + "'" +
            ", super ='' " + super.toString() + "'"+
            "}";
    }

    @Override
    public boolean isStateCorrect(DeviceState state) {
        return state == DeviceState.UP || state == DeviceState.DOWN || state == DeviceState.NOTKNOW;
    }

}
