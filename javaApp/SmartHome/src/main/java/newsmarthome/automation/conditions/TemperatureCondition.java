package newsmarthome.automation.conditions;

import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import newsmarthome.model.hardware.sensor.Termometr;

/**
 * A condition that checks the temperature value of a thermometer sensor against a given value or range of values.
 * Extends the SensorCondition class with a Float type parameter.
 * @see SensorCondition
 */
public class TemperatureCondition extends SensorCondition<Float>{
    Logger logger;

    /**
     * Constructor for a TemperatureCondition object with a single value to check against.
     * @param sensor The thermometer sensor to check the temperature value of.
     * @param valueCheckType The type of check to perform (e.g. EQUAL, GREATER, LESS).
     * @param value The value to check the temperature against.
     */
    public TemperatureCondition(Termometr sensor, ValueCheckType valueCheckType, Float value) {
        super(sensor, valueCheckType, value);
        logger = LoggerFactory.getLogger(TemperatureCondition.class);
    }

    /**
     * Constructor for a TemperatureCondition object with a range of values to check against.
     * @param sensor The thermometer sensor to check the temperature value of.
     * @param value The lower bound of the range to check the temperature against.
     * @param value2 The upper bound of the range to check the temperature against.
     */
    public TemperatureCondition(Termometr sensor, Float value, Float value2) {
        super(sensor, value, value2);
        LoggerFactory.getLogger(TemperatureCondition.class);
    }

    /**
     * Checks if the temperature value of the sensor satisfies the condition.
     * @return True if the condition is satisfied, false otherwise.
     */
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
