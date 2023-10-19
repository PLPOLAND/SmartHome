package newsmarthome.automation.conditions;

import newsmarthome.model.hardware.sensor.Sensor;

public abstract class SensorCondition<T> implements Condition {

    protected SensorCondition(Sensor sensor, ValueCheckType valueCheckType, T value) {
        this.sensor = sensor;
        this.valueCheckType = valueCheckType;
        this.value = value;
    }

    protected SensorCondition(Sensor sensor, ValueCheckType valueCheckType, T value, T value2) {
        this.sensor = sensor;
        this.valueCheckType = valueCheckType;
        this.value = value;
        this.value2 = value2;
    }

    /** 
     * Urządzenie którego stan należy sprawdzić 
     */
    Sensor sensor;
    
    /** 
     * Typ sprawdzania wartości 
    */
    ValueCheckType valueCheckType;
    /** 
     * Wartość "graniczna" dla warunków jedno-wartościowych i przedziałowych 
     */
    T value;
    /** 
     * Druga wartość "graniczna dla warunków przedziałowych" 
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
