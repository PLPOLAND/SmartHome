package newsmarthome.model.hardware.sensor;

import java.util.Arrays;

public enum SensorsTypes {
    NONE,
    THERMOMETR,
    THERMOMETR_HYGROMETR,
    TWILIGHT,
    MOTION,
    BUTTON;

    public static String[] getNames() {
        return Arrays.stream(SensorsTypes.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
    public static SensorsTypes fromString(String type) {
        type = type.toUpperCase();
        switch (type) {
            case "THERMOMETR":
                return THERMOMETR;
            case "THERMOMETR_HYGROMETR":
                return THERMOMETR_HYGROMETR;
            case "TWILIGHT":
                return TWILIGHT;
            case "MOTION":
                return MOTION;
            case "BUTTON":
                return BUTTON;
            default:
                return NONE;
        }
    }
}
