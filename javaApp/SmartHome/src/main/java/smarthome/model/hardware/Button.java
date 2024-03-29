package smarthome.model.hardware;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.slf4j.LoggerFactory;

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

    public Button funkcjeKlikniec(List<ButtonLocalFunction> funkcjeKlikniec) {
        for (ButtonLocalFunction function : funkcjeKlikniec) {
            this.addFunkcjaKilkniecia(function);
        }
        return this;
    }
    public void setFunkcjeKlikniec(List<ButtonLocalFunction> funkcjeKlikniec) {
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
