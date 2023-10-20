package newsmarthome.automation.conditions;

import newsmarthome.automation.conditions.Condition;
//TODO implement
//TODO uzależnić od warunku urządzenia lub sensora
//TODO sprawdzać czas w osobnym wątku i zmieniać stan warunku
public class TimeCondition implements Condition{

    static final ConditionType conditionType = ConditionType.TIME; 
    

    @Override
    public boolean checkCondition() {
        return false;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }
    

}
