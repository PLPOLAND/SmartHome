package newsmarthome.automation.conditions;

import newsmarthome.model.hardware.sensor.Sensor;

/**
 * An abstract class representing a condition that can be checked against a sensor's value.
 * A SensorCondition can be constructed with either a single value to check against or a range of values.
 * The type of value check to perform is determined by the ValueCheckType enum.
 * @param <T> the type of value to check against
 */
public abstract class SensorCondition<T> implements Condition {

    /**
     * Constructs a SensorCondition with a single value to check against.
     * @param sensor the sensor to check
     * @param valueCheckType the type of value check to perform
     * @param value the value to check against
     */
    protected SensorCondition(Sensor sensor, ValueCheckType valueCheckType, T value) {
        this.sensor = sensor;
        this.valueCheckType = valueCheckType;
        this.value = value;
    }

    /**
     * Constructs a SensorCondition with a range of values to check against.
     * @param sensor the sensor to check
     * @param value the lower bound of the range to check against
     * @param value2 the upper bound of the range to check against
     */
    protected SensorCondition(Sensor sensor, T value, T value2) {
        this.sensor = sensor;
        this.valueCheckType = ValueCheckType.IN_RANGE;
        this.value = value;
        this.value2 = value2;
    }

    /** 
     * The sensor to check.
     */
    Sensor sensor;
    
    /** 
     * The type of value check to perform.
     */
    ValueCheckType valueCheckType;
    
    /** 
     * The value to check against for single-value checks or the first bound of the range for range checks.
     */
    T value;
    
    /** 
     * The second bound of the range to check against for range checks.
     */
    T value2;
    
    /**
     * An enum representing the different types of value checks that can be performed in a SensorCondition.
     */
    enum ValueCheckType {
        EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, IN_RANGE;

        /**
         * Returns a string representation of the ValueCheckType.
         *
         * @return a string representation of the ValueCheckType
         */
        @Override
        public String toString() {
            switch (this) {
                case EQUAL:
                    return "EQUAL";
                case NOT_EQUAL:
                    return "NOT_EQUAL";
                case GREATER:
                    return "GREATER";
                case LESS:
                    return "LESS";
                case GREATER_OR_EQUAL:
                    return "GREATER_OR_EQUAL";
                case LESS_OR_EQUAL:
                    return "LESS_OR_EQUAL";
                case IN_RANGE:
                    return "IN_RANGE";
                default:
                    return "UNKNOWN";
            }
        }

        /**
         * Returns the ValueCheckType corresponding to the given string.
         *
         * @param s the string representation of the ValueCheckType
         * @return the ValueCheckType corresponding to the given string, or null if no match is found
         */
        public static ValueCheckType fromString(String s) {
            s= s.toUpperCase();
            switch (s) {
                case "EQUAL":
                    return EQUAL;
                case "NOT_EQUAL":
                    return NOT_EQUAL;
                case "GREATER":
                    return GREATER;
                case "LESS":
                    return LESS;
                case "GREATER_OR_EQUAL":
                    return GREATER_OR_EQUAL;
                case "LESS_OR_EQUAL":
                    return LESS_OR_EQUAL;
                case "IN_RANGE":
                    return IN_RANGE;
                default:
                    return null;
            }
        }
    }

}
