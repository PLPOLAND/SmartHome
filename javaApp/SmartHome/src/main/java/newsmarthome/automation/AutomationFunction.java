package newsmarthome.automation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import newsmarthome.automation.conditions.Condition;
import newsmarthome.exception.HardwareException;

/**
 * AutomationFunction
 * funkcja reagująca na warunki
 */
public class AutomationFunction extends Function {

    /**
     * true - funkcja jest wykonywana tylko przy zajściu warunku.
     * false - funkcja jest wykonywana przy zajściu warunku i przy końcu jego zachodzenia.
     */
    boolean oneWay; 

    List<Condition> conditions;



    public AutomationFunction() {
        super( FunctionType.AUTOMATION );
        oneWay = false;
        conditions = null;
    }

    @JsonIgnore
    public List<Condition> getConditions() {
        return conditions;
    }

    public void setCondition(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    /**
     * Check if all conditions are met.
     * @return true if all conditions are met, false otherwise
     */
    private boolean checkConditions(){
        for (Condition condition : this.conditions) {
            if (!condition.checkCondition()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if any condition is met. If so, activate the function.
     */
    @Override
    public void run() throws HardwareException {
        
        if (oneWay) {
            if (!this.isActive() && checkConditions()) {
                this.activate();
            }
        }
        else {
            if (!this.isActive() && checkConditions()) {
                this.activate();
            }
            else if( this.isActive() && !checkConditions()) {
                this.deactivate();
            }
        }
        
    }

    @Override
    public void activate() throws HardwareException {
        for (FunctionAction action : this.actions) {
            action.activate();
        }
        
    }

    @Override
    public void deactivate() throws HardwareException {
        for (FunctionAction action : this.actions) {
            action.deactivate();
        }
    }

}
