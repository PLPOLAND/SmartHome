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
    ArrayList<ButtonFunction> funkcjeKlikniec;

    
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

    public Button funkcjeKlikniec(List<ButtonFunction> funkcjeKlikniec) {
        for (ButtonFunction function : funkcjeKlikniec) {
            this.addFunkcjaKilkniecia(function);
        }
        return this;
    }
    public void setFunkcjeKlikniec(List<ButtonFunction> funkcjeKlikniec) {
        for (ButtonFunction function : funkcjeKlikniec) {
            this.addFunkcjaKilkniecia(function);
        }
    }

    public void addFunkcjaKilkniecia(ButtonFunction fun){
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
    
    public List<ButtonFunction> getFunkcjeKlikniec(){
        return this.funkcjeKlikniec;
    }
    
    public int getPin() {
        return this.pin;
    }
    
    public void setPin(int pin) {
        this.pin = pin;
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
