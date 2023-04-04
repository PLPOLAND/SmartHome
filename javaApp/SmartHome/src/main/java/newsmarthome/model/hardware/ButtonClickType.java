package newsmarthome.model.hardware;

import java.util.Arrays;

public enum ButtonClickType{
    CLICKED,
    HOLDING,
    HOLDED;

    public static String[] getNames() {
        return Arrays.stream(ButtonClickType.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}