package newsmarthome.wifi;

import java.util.ArrayList;
import java.util.List;

import newsmarthome.i2c.BaseConverter;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Termometr;

public class WiFiMasterToSlaveConverter extends BaseConverter {

	@Override
	public void changeBlindState(Blind blind, DeviceState state) {
		// Implementation here
	}

	@Override
	public Float checkTemperature(Termometr termometr) {
		// Implementation here
        return 0.0f;
	}

	@Override
	public int addHigrometr(Higrometr higrometr) {
		// Implementation here
        return 0;
	}

	@Override
	public List<String> getSlavesAdresses() {
		// Implementation here
		return new ArrayList<>();
	}

	@Override
	public boolean checkAndReinitBoard(int boardId) {
		// Implementation here
        return false;
	}

	@Override
	public int[] addTermometr(int termometrId) {
		// Implementation here
        return new int[0];
	}

	@Override
	public int howManyThermometersOnSlave(int slaveId) {
		// Implementation here
		return 0;
	}

	@Override
	public byte[] checkHighrometr(Higrometr higrometr) {
		// Implementation here
        return new byte[0];
	}

	@Override
	public int sendClickFunction(ButtonLocalFunction function) {
		// Implementation here
        return 0;
	}

	@Override
	public int addUrzadzenie(Device device) {
		// Implementation here
        return 0;
	}

	@Override
	public int checkDeviceState(int deviceId, int stateId) {
		// Implementation here
        return 0;
	}

	@Override
	public boolean reInitBoard(int boardId) {
		// Implementation here
        return false;
	}

	@Override
	public boolean checkInitOfBoard(int boardId) {
		// Implementation here
        return false;
	}

	@Override
	public int sendRemoveFunction(int functionId, int deviceId) {
		// Implementation here
        return 0;
	}

	@Override
	public boolean isSlaveConnected(int slaveId) {
		// Implementation here
		return false;
	}

	@Override
	public void changeSwitchState(int switchId, int stateId, DeviceState state) {
		// Implementation here
	}

	@Override
	public int addPrzycisk(Button button) {
		// Implementation here
        return 0;
	}
}
