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
	// #region Komendy
	/** [S,U] */
	private static final byte[] STATUS_URZADZEN = { 'S', 'U' };
	/** [S,R] */
	private static final byte[] STATUS_RGB = { 'S', 'R' };
	/** [W] */
	private static final byte[] CHECK_TO_WORK = { 'W' };
	/** [I] */
	private static final byte[] CHECK_INIT = { 'I' };
	/** [R] */
	private static final byte[] REINIT = { 'R' };
	/** [U,S] */
	private static final byte[] ZMIEN_STAN_PRZEKAZNIKA = { 'U', 'S' }; // + id + stan
	/** [U,B] */
	private static final byte[] ZMIEN_STAN_ROLETY = { 'U', 'B' }; // + id + stan
	/** [T] */
	private static final byte[] POBIERZ_TEMPERATURE = { 'T' }; // + ADRESS (8byte)
	/** [H] */
	private static final byte[] POBIERZ_TEMPERATURE_I_WILGOTNOSC = { 'H' }; // + id
	/** [A, S] */
	private static final byte[] DODAJ_URZADZENIE = { 'A', 'S' }; // + PIN
	/** [A, R] */
	private static final byte[] DODAJ_ROLETE = { 'A', 'R' }; // + PIN + PIN
	/** [A, P] */
	private static final byte[] DODAJ_PRZYCISK = { 'A', 'P' }; // + PIN
	/** [A, T] */
	private static final byte[] DODAJ_TERMOMETR = { 'A', 'T' };
	/** [A, H] */
	private static final byte[] DODAJ_HIGROMETR = { 'A', 'H' };
	/** [P, K, L] */
	private static final byte[] DODAJ_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L' };
	/** [P, K, L, D] */
	private static final byte[] USUN_LOKALNA_FUNKCJE_KLIKNIEC = { 'P', 'K', 'L', 'D' };
	/** [S, D] */
	private static final byte[] SPRAWDZ_STAN_URZADZENIA = { 'S', 'D' };
	/** [C, T, N] */
	private static final byte[] ILE_TERMOMETROW = { 'C', 'T', 'N' };
	/** [W] */
	private static final byte[] SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA = { 'W' };
	/** [G] */
	private static final byte[] ODBIERZ_KOMENDE = { 'G' };
	// #endregion

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
		return wifiSlaveRepository.findByConnected(true).stream().map(WifiSlave::getId).toList();
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
		if (slave.isPresent()) {
			return slave.get().isConnected();
		}
		else {
			return false;
		}
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
