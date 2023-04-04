package newsmarthome.model.hardware;

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
}
