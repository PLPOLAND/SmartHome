package newsmarthome.service;

import newsmarthome.model.hardware.sensor.ButtonLocalFunction;

/** Lokalna funkcja kliknięcia przycisku w żądaniu konfiguracji: N kliknięć -> urządzenie -> akcja. */
public class ClickFunctionSpec {

    private final int clicks;
    private final int deviceId;
    private final ButtonLocalFunction.State state;

    public ClickFunctionSpec(int clicks, int deviceId, ButtonLocalFunction.State state) {
        this.clicks = clicks;
        this.deviceId = deviceId;
        this.state = state;
    }

    public int getClicks() {
        return clicks;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public ButtonLocalFunction.State getState() {
        return state;
    }
}
