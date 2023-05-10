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
}
