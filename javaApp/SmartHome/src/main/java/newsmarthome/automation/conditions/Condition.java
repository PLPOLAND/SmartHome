package newsmarthome.automation.conditions;

/**
 * Condition
 * interfejs warunku
 */
public interface Condition {
    /**
     * Sprawdza czy warunek jest spełniony
     * @return true - warunek jest spełniony, false - nie jest spełniony
     */
    public abstract boolean checkCondition();

    /**
     * Zwraca typ warunku
     * @return typ warunku
     */
    public abstract ConditionType getConditionType();

}

enum ConditionType {
    TIME, SENSOR, DEVICE, CLOCK_SPAN
}