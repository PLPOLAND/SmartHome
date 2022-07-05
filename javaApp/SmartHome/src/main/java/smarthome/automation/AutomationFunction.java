package smarthome.automation;

import smarthome.exception.HardwareException;

public class AutomationFunction extends Function {

    /**
     * true - funkcja jest wykonywana tylko przy zajściu warunku.
     * false - funkcja jest wykonywana przy zajściu warunku i przy końcu jego zachodzenia.
     */
    boolean oneWay; 

    AutomationCondition condition;



    public AutomationFunction() {
        super();
        oneWay = false;
        condition = null;
    }

    public AutomationCondition getCondition() {
        return condition;
    }

    public void setCondition(AutomationCondition condition) {
        this.condition = condition;
    }

    public void addCondition(Condition condition) {
        this.condition.addCondition(condition);
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    /**
     * Sprawdza czy zaszedł warunek i wykonuje akcje.
     */
    @Override
    public void run() throws HardwareException {
        
        if (oneWay) {
            if (!this.isActive() && this.condition.checkCondition()) {
                    this.activate();
            }
        }
        else {
            if (!this.isActive() && this.condition.checkCondition()) {
                this.activate();
            }
            else if( this.isActive() && !this.condition.checkCondition()) {
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
