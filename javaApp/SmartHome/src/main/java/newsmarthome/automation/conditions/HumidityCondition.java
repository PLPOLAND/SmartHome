package newsmarthome.automation.conditions;

import newsmarthome.model.hardware.sensor.Higrometr;

/**
 * A condition that checks the humidity level of a Higrometr sensor.
 * The condition can check if the humidity level is equal, not equal, greater, less, greater or equal, less or equal, or within a range of values.
 * @see SensorCondition
 */
public class HumidityCondition extends SensorCondition<Integer>{

    /**
     * Constructs a new HumidityCondition object with a Higrometr sensor, a value check type, and a value.
     * @param sensor the Higrometr sensor to check the humidity level of
     * @param valueCheckType the type of value check to perform (equal, not equal, greater, less, greater or equal, less or equal, or within a range of values)
     * @param value the value to check against the humidity level of the sensor
     */
    public HumidityCondition(Higrometr sensor,ValueCheckType valueCheckType, Integer value) {
        super(sensor, valueCheckType, value);
    }

    /**
     * Constructs a new HumidityCondition object with a Higrometr sensor, a minimum value, and a maximum value.
     * @param sensor the Higrometr sensor to check the humidity level of
     * @param value the minimum value to check against the humidity level of the sensor
     * @param value2 the maximum value to check against the humidity level of the sensor
     */
    public HumidityCondition(Higrometr sensor, Integer value, Integer value2) {
        super(sensor, value, value2);
    }

    /**
     * Checks if the humidity level of the sensor meets the condition.
     * @return true if the humidity level meets the condition, false otherwise
     */
    @Override
    public boolean checkCondition() {
        Integer sensorValue = ((Higrometr) sensor).getHumidity();
        switch (valueCheckType) {
            case EQUAL:
                return sensorValue.intValue() == value.intValue();
            case NOT_EQUAL:
                return sensorValue.intValue() != value.intValue();
            case GREATER:
                return sensorValue.intValue() > value.intValue();
            case LESS:
                return sensorValue.intValue() < value.intValue();
            case GREATER_OR_EQUAL:
                return sensorValue.intValue() >= value.intValue();
            case LESS_OR_EQUAL:
                return sensorValue.intValue() <= value.intValue();
            case IN_RANGE:
                return sensorValue.intValue() >= Math.min(value.intValue(), value2.intValue()) && sensorValue.intValue() <= Math.max(value.intValue(), value2.intValue());
            default:
                return false;
        }
    }
}
