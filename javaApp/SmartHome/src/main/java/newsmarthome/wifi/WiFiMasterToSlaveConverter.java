package newsmarthome.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import newsmarthome.database.repository.WifiSlaveRepository;
import newsmarthome.i2c.BaseConverter;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
import newsmarthome.model.hardware.sensor.Termometr;

@Service
public class WiFiMasterToSlaveConverter extends BaseConverter {

	@Autowired
	WifiSlaveRepository wifiSlaveRepository;

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
	public List<Integer> getSlavesAdresses() {
		// return wifiSlaveRepository.findByConnected(true).stream().map(slave -> slave.id ).toList();
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
		Optional<WifiSlave> slave = wifiSlaveRepository.findById(slaveId);
		// if (slave.isEmpty()) {
		// 	return false;
		// }
		//TODO check if slave is still connected
		return slave.get().isConnected();
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
