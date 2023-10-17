package newsmarthome.automation.conditions;

import java.util.ArrayList;
import java.util.List;

/*
 * Klasa reprezentująca warunek, który musi być spełniony aby wykonać akcje.
 * Pozwala na dodawanie wielu warunków, które muszą być spełnione.
 */
public class AutomationCondition implements Condition {
    
    ArrayList<Condition> conditions;

    public AutomationCondition() {
        conditions = new ArrayList<>();
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public boolean checkCondition() {
        boolean result = true;
        for (Condition condition : conditions) {
            result = condition.checkCondition();
            if (!result) {
                break;
            }
        }
        return result;
    }

}
