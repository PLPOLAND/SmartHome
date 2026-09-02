package newsmarthome.automation.conditions;

/**
 * Class for chcecking if the condition is true for time
 */
public class TimeCondition implements Condition{

    static final ConditionType conditionType = ConditionType.TIME; 
    
    /** Condition to check */
    Condition condition;

    /** Time in millis when the condition started to be true */
    long startTime;
    /** Time in millis needed to elapsed for condition to be true*/
    long conditionTime;

    /** Holds information if the condition is true for condition time */
    boolean conditionState;

    /**
     * Constructor
     * @param condition condition to check
     * @param conditionTime time in millis needed to elapsed for condition to be true
     */
    TimeCondition(Condition condition, long conditionTime){
        this.condition = condition;
        this.conditionTime = conditionTime;
        startTime = 0;
        conditionState = false;
    }

    /**
     * Check if the condition is true for condition time.
     * Check if the condition is true. If it is true, check if the condition time elapsed.
     * @return the condition state
     */
    public boolean chceckConditionTime(){
        if(condition.checkCondition()){
            if(startTime == 0){
                startTime = System.currentTimeMillis();
            }
            if(System.currentTimeMillis() - startTime >= conditionTime){
                conditionState = true;
            }
        }else{
            startTime = 0;
            conditionState = false;
        }
        return conditionState;
    }

    @Override
    public boolean checkCondition() {
        return chceckConditionTime();
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }
    

}
