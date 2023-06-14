package newsmarthome.model.hardware.device;

import java.util.Arrays;

public enum DeviceState{
    DOWN,
    NOTKNOW,
    UP,
    OFF,
    ON;

    public static String[] getNames() {
        return Arrays.stream(DeviceState.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
    
    public static DeviceState fromString(String text) {
        switch (text) {
            case "down":
                return DOWN;
            case "notknow":
            case "middle":
                return NOTKNOW;
            case "up":
                return UP;
            case "off":
                return OFF;
            case "on":
                return ON;
            default:
                return null;
        }
    }

}

