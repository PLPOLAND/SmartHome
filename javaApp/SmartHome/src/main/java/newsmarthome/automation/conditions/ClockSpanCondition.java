package newsmarthome.automation.conditions;

import java.time.LocalTime;

/**
 * A condition that checks if the current time is within a specified time span.
 */
public class ClockSpanCondition implements Condition{

    static final ConditionType conditionType = ConditionType.CLOCK_SPAN;

    /** Start time when the condition become valid */
    final LocalTime startTime;
    /** End time when the condition become invalid */
    final LocalTime endTime;

    /**
     * Constructs a new ClockSpanCondition object with the specified start and end times.
     * @param startTime the start time of the time span
     * @param endTime the end time of the time span
     */
    public ClockSpanCondition(LocalTime startTime, LocalTime endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Checks if the current time is within the specified time span.
     * @return true if the current time is within the time span, false otherwise
     */
    @Override
    public boolean checkCondition() {
        return LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime);
    }

    /**
     * Returns the type of this condition.
     * @return the type of this condition
     */
    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }
    
}
