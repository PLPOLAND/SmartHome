package newsmarthome.model.hardware.sensor;

import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import newsmarthome.exception.HardwareException;
import newsmarthome.exception.SoftwareException;

@Component
@Scope("prototype")
public class Higrometr extends Termometr{
    Integer humidity;
    Integer maxHumidity;
    Integer minHumidity;

    public Higrometr(){
        super();
        this.typ = SensorsTypes.THERMOMETR_HYGROMETR;
        this.humidity = Integer.MAX_VALUE;
        this.maxHumidity = Integer.MIN_VALUE;
        this.minHumidity = Integer.MAX_VALUE;
        logger = LoggerFactory.getLogger(Higrometr.class);
    }

    public Higrometr(int slaveID){
        super(slaveID);
        this.typ = SensorsTypes.THERMOMETR_HYGROMETR;
        this.humidity = Integer.MAX_VALUE;
        this.maxHumidity = Integer.MIN_VALUE;
        this.minHumidity = Integer.MAX_VALUE;
        logger = LoggerFactory.getLogger(Higrometr.class);
    }

    public Integer getHumidity() {
        return this.maxHumidity;
    }
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
        updateMaxMinHumidity();
    }

    public Integer getMaxHumidity() {
        return this.maxHumidity;
    }

    public Integer getMinHumidity() {
        return this.minHumidity;
    }

    public void clearMaxMinHumidity() {
        this.maxHumidity = Integer.MIN_VALUE;
        this.minHumidity = Integer.MAX_VALUE;
    }

    public void updateMaxMinHumidity() {
        if (this.humidity > this.maxHumidity) {
            this.maxHumidity = this.humidity;
        }
        if (this.humidity < this.minHumidity) {
            this.minHumidity = this.humidity;
        }
    }

    @Override
    public void update(){
        byte[] response = null;
        try {
             response = slaveSender.checkHighrometr(this);
            
            String tmp ="";
            for (int i = 0; i < 5; i++) {
                byte b = response[i];
                if (b >= 48 && b<= 57 || b == '.') {
                    tmp += (char) b;
                }
            }
            this.setTemperatura(Float.parseFloat(tmp));
            this.setHumidity((int) response[5]);
        } catch (HardwareException|SoftwareException e) {
            logger.error("Nie udało się zaktualizować higrometru: {}", e.getMessage());
        } catch (NumberFormatException e) {
            
            logger.error("Wartość temperatury nie jest liczbą: {}", e.getMessage());
            if (response != null) {
                logger.error("Otrzymana odpowiedź: {}", Arrays.toString(response));
            }
        }

    }

    public void configure(){
        try{
            this.setOnSlaveID(slaveSender.addHigrometr(this));
        }
        catch(HardwareException|SoftwareException e){
            logger.error("Nie udało się skonfigurować higrometru!: {}", e.getMessage());
        }

    }
}
