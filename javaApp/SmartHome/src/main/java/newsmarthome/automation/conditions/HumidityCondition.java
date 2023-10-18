package newsmarthome.automation.conditions;

import newsmarthome.model.hardware.sensor.Higrometr;

public class HumidityCondition extends SensorCondition<Integer>{
    public HumidityCondition(Higrometr sensor,ValueCheckType valueCheckType, Integer value) {
        super(sensor, valueCheckType, value);
    }

    public HumidityCondition(Higrometr sensor, ValueCheckType valueCheckType,Integer value, Integer value2) {
        super(sensor, valueCheckType, value, value2);
    }
    @Override
    public boolean checkCondition() {
        Integer sensorValue = ((Higrometr) sensor).getHumidity();
        switch (valueCheckType) {
            case EQUAL:
                return sensorValue.intValue() == value.intValue();
            case NOT_EQUAL:
                return sensorValue.intValue() != value.intValue();
            case GREATER:
                return sensorValue.intValue() > value.intValue();
            case LESS:
                return sensorValue.intValue() < value.intValue();
            case GREATER_OR_EQUAL:
                return sensorValue.intValue() >= value.intValue();
            case LESS_OR_EQUAL:
                return sensorValue.intValue() <= value.intValue();
            case IN_RANGE:
                return sensorValue.intValue() >= Math.min(value.intValue(), value2.intValue()) && sensorValue.intValue() <= Math.max(value.intValue(), value2.intValue());
            default:
                return false;
        }
    }
}
