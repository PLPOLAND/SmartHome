package smarthome.model.hardware;

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
    
}

