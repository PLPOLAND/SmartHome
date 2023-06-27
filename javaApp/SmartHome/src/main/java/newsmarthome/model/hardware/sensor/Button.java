package newsmarthome.model.hardware.sensor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import newsmarthome.exception.HardwareException;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope("prototype")
public class Button extends Sensor{
    

    /** Numer pinu do sterowania przekaznikiem na Slavie */
    private int pin;
    
    @JsonManagedReference
    /**Przechowuje funkcje kliknięć danego przycisku */
    ArrayList<ButtonLocalFunction> funkcjeKlikniec;

    
    public Button(){
        super(SensorsTypes.BUTTON);
        funkcjeKlikniec = new ArrayList<>();
        logger = LoggerFactory.getLogger(Button.class);
    }
    
    public Button(int slaveID){
        super(slaveID, SensorsTypes.BUTTON);
        funkcjeKlikniec = new ArrayList<>();
        logger = LoggerFactory.getLogger(Button.class);
        this.pin = -1;
    }
    public Button(int slaveID, int pin){
        super(slaveID, SensorsTypes.BUTTON);
        funkcjeKlikniec = new ArrayList<>();   
        logger = LoggerFactory.getLogger(Button.class);
        this.pin = pin;
    }
    /**
     * Konfiguruje przycisk na slave-a
     */
    public void configure(){
        logger.debug("Wysyłanie konfiguracji przycisku na slave-a o id: {}", this.getSlaveAdress());
        try {
            this.setOnSlaveID(slaveSender.addPrzycisk(this));
            for (ButtonLocalFunction buttonLocalFunction : funkcjeKlikniec) {
                slaveSender.sendClickFunction(buttonLocalFunction);
            }
        } catch (HardwareException e) {
            logger.error("Błąd podczas konfiguracji przycisku na slave-a o id: {}! error: {}", this.getSlaveAdress(), e.getMessage());
            // e.printStackTrace();
        }
    }

    public void setFunkcjeKlikniec(List<ButtonLocalFunction> funkcjeKlikniec) {
        this.funkcjeKlikniec = (ArrayList<ButtonLocalFunction>)funkcjeKlikniec;
    }
    public void addFunkcjeKlikniec(List<ButtonLocalFunction> funkcjeKlikniec) {
        for (ButtonLocalFunction function : funkcjeKlikniec) {
            this.addFunkcjaKilkniecia(function);
        }
    }

    public void addFunkcjaKilkniecia(ButtonLocalFunction fun){
        fun.setButton(this);
        this.funkcjeKlikniec.add(fun);
    }
    public void removeFunkcjaKilkniecia(int numberOfClicks){
        for (int i = 0; i < funkcjeKlikniec.size(); i++) {
            if (funkcjeKlikniec.get(i).getClicks() == numberOfClicks) {
                funkcjeKlikniec.remove(i);
                break;
            }
        }
    }
    
    public List<ButtonLocalFunction> getFunkcjeKlikniec(){
        return this.funkcjeKlikniec;
    }
    
    public int getPin() {
        return this.pin;
    }
    
    public void setPin(int pin) {
        this.pin = pin;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Button other = (Button) obj;
        if (this.pin != other.pin) {
            return false;
        }
        if (this.getId() != other.getId()) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "{" +
        " pin='" + getPin() + "'" +
        ", funkcjeKlikniec='" + getFunkcjeKlikniec() + "'" +
        ", super= '" + super.toString()+
            "}";
    }
    
    

}
