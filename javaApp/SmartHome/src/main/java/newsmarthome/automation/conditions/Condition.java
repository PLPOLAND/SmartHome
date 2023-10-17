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

}
