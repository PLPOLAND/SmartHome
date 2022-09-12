package smarthome.automation;

import java.util.ArrayList;
import java.util.List;

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
