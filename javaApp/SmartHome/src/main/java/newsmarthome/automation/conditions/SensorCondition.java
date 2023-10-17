package newsmarthome.automation.conditions;

import smarthome.model.hardware.Sensor;

public class SensorCondition implements Condition {

    /** Urządzenie którego stan należy sprawdzić */
    Sensor sensor;
    
    /** Stan urządzenia w którym warunek jest prawdziwy */
    //TODO zrobić osobne klasy dla różnych typów sensorów, np. TemperatureCondition, HumidityCondition itp.
    //TODO dodać możliwość sprawdzania czy wartość jest większa/mniejsza/równa od zadanej
    //TODO dodać możliwość sprawdzania czy wartość jest w przedziale
    //TODO dodać możliwość sprawdzania czy wartość jest równa zadanej
    //TODO dodać możliwość sprawdzania czy wartość jest różna od zadanej



    @Override
    public boolean checkCondition() {
        // TODO dodać warunek! itd.

        
        return false;
    }

}
