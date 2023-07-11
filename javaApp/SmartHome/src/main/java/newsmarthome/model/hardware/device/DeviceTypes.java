package newsmarthome.model.hardware.device;

import java.util.Arrays;

public enum DeviceTypes {
    NONE, 
    LIGHT,
    GNIAZDKO,
    BLIND,
    WENTYLATOR;

    public static String[] getNames() {
        return Arrays.stream(DeviceTypes.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    public static DeviceTypes fromString(String type) {
        type = type.toUpperCase();
        switch (type) {
            case "LIGHT":
                return LIGHT;
            case "OUTLET":
            case "GNIAZDKO":
                return GNIAZDKO;
            case "BLIND":
                return BLIND;
            case "FAN":
            case "WENTYLATOR":
                return WENTYLATOR;
            default:
                return NONE;
        }
    }
}
