package newsmarthome.automation.conditions;

import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import newsmarthome.model.hardware.sensor.Termometr;

public class TemperatureCondition extends SensorCondition<Float>{
    Logger logger;

    public TemperatureCondition(Termometr sensor, ValueCheckType valueCheckType, Float value) {
        super(sensor, valueCheckType, value);
        logger = LoggerFactory.getLogger(TemperatureCondition.class);
        
    }

    public TemperatureCondition(Termometr sensor, ValueCheckType valueCheckType, Float value, Float value2) {
        super(sensor, valueCheckType, value, value2);
        LoggerFactory.getLogger(TemperatureCondition.class);
    }

    @Override
    public boolean checkCondition() {
        Float sensorValue = ((Termometr) sensor).getTemperatura();
        switch (valueCheckType) {
            case EQUAL:
                return sensorValue.floatValue() == value.floatValue();
            case NOT_EQUAL:
                return sensorValue.floatValue() != value.floatValue();
            case GREATER:
                return sensorValue.floatValue() > value.floatValue();
            case LESS:
                return sensorValue.floatValue() < value.floatValue();
            case GREATER_OR_EQUAL:
                return sensorValue.floatValue() >= value.floatValue();
            case LESS_OR_EQUAL:
                return sensorValue.floatValue() <= value.floatValue();
            case IN_RANGE:
                return sensorValue.floatValue() >= Math.min(value.floatValue(), value2.floatValue()) && sensorValue.floatValue() <= Math.max(value.floatValue(), value2.floatValue());
            default:
                return false;
        }
    }
    
}
