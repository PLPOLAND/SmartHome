package newsmarthome.model.response;

import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;

public class DeviceStateResponse {
    private int id;
    private DeviceState state;

    public DeviceStateResponse(int id, DeviceState state) {
        this.id = id;
        this.state = state;
    }

    public DeviceStateResponse(Device device) {
        this.id = device.getId();
        this.state = device.getState();
    }

    protected DeviceStateResponse() {
    }

    public int getId() {
        return id;
    }

    public DeviceState getState() {
        return state;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }


}
