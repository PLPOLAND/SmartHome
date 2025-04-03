package newsmarthome.wifi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import newsmarthome.database.WifiSlaveDAO;
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
@Log4j2
public class WiFiMasterToSlaveConverter extends BaseConverter {
	// #region Komendy
	/** [S,U] */
	private static final String STATUS_URZADZEN = "SU";
	private static final String STATUS_RGB = "SR";
	private static final String CHECK_TO_WORK = "W";
	private static final String CHECK_INIT = "I";
	private static final String REINIT = "R";
	private static final String ZMIEN_STAN_PRZEKAZNIKA = "US"; // + id + stan
	private static final String ZMIEN_STAN_ROLETY = "UB"; // + id + stan
	private static final String POBIERZ_TEMPERATURE = "T"; // + ADRESS (8byte)
	private static final String POBIERZ_TEMPERATURE_I_WILGOTNOSC = "H"; // + id
	private static final String DODAJ_URZADZENIE = "AS"; // + PIN
	private static final String DODAJ_ROLETE = "AR"; // + PIN + PIN
	private static final String DODAJ_PRZYCISK = "AP"; // + PIN
	private static final String DODAJ_TERMOMETR = "AT";
	private static final String DODAJ_HIGROMETR = "AH";
	private static final String DODAJ_LOKALNA_FUNKCJE_KLIKNIEC = "PKL";
	private static final String USUN_LOKALNA_FUNKCJE_KLIKNIEC = "PKLD";
	private static final String SPRAWDZ_STAN_URZADZENIA = "SD";
	private static final String ILE_TERMOMETROW = "CTN";
	private static final String SPRAWDZ_CZY_JEST_COS_DO_WYSLANIA = "W";
	private static final String ODBIERZ_KOMENDE = "G";
	// #endregion

	private final WifiSlaveRepository wifiSlaveRepository;
	private final WifiSlaveDAO wifiSlaveDAO;

	@Autowired
	public WiFiMasterToSlaveConverter(WifiSlaveRepository wifiSlaveRepository, WifiSlaveDAO wifiSlaveDAO) {
		this.wifiSlaveRepository = wifiSlaveRepository;
		this.wifiSlaveDAO = wifiSlaveDAO;
	}

	List<Command> commands = new ArrayList<>();

	void gotCommand(String inString) {
		String[] parts = inString.split(";");
		if (parts.length != 2) {
			log.error("Got wrong command from slave: " + inString);
			return;
		}
		commands.stream().filter(c -> c.getId() == Integer.parseInt(parts[0])).findFirst().ifPresentOrElse(command -> {
			command.setIn(parts[1]);
			command.setState(CommandState.OK);
		}, () -> {
			Command command = new Command();
			command.setId(Integer.parseInt(parts[0]));
			command.setIn(parts[1]);
			command.setState(CommandState.NEW);
			commands.add(command);
		});
	}

	@Override
	public void changeBlindState(Blind blind, DeviceState state) {
		String command = ZMIEN_STAN_ROLETY + blind.getId() + state.toCommandString();
		Command cmd = new Command();
		cmd.setOut(command);
		cmd.setType(CommandType.CHANGEBLINDSTATE);
		cmd.setState(CommandState.WAITING);
		commands.add(cmd);
		log.debug("Command added to list: " + command);
		log.debug("Sending command to slave: " + command);
		WifiSlave slave = wifiSlaveDAO.getWifiSlaveById(blind.getSlaveID());
		if (slave != null) {
			slave.getOut().println(cmd.sendString());
		} else {
			log.error("Slave not found for blind: " + blind.getId());
			return;
		}
		while (cmd.getState() != CommandState.OK) {
			try {
				Thread.sleep(10); // Wait for 10ms before checking again
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("Thread interrupted while waiting for command to be OK", e);
				break;
			}
		}
		if (cmd.getIn().equals("O")) {
			log.info("Command executed successfully: " + command);
		} else {
			log.error("Command execution failed: " + command);
		}
	}

	@Override
	public Float checkTemperature(Termometr termometr) {
		// TODO
		return 0.0f;
	}

	@Override
	public int addHigrometr(Higrometr higrometr) {
		// TODO
		return 0;
	}

	@Override
	public List<Integer> getSlavesAdresses() {
		return wifiSlaveRepository.findByConnected(true).stream().map(WifiSlave::getId).toList();
	}

	@Override
	public boolean checkAndReinitBoard(int boardId) {
		// TODO
		return false;
	}

	@Override
	public int[] addTermometr(int termometrId) {
		// TODO
		return new int[0];
	}

	@Override
	public int howManyThermometersOnSlave(int slaveId) {
		// TODO
		return 0;
	}

	@Override
	public byte[] checkHighrometr(Higrometr higrometr) {
		// TODO
		return new byte[0];
	}

	@Override
	public int sendClickFunction(ButtonLocalFunction function) {
		// TODO
		return 0;
	}

	@Override
	public int addUrzadzenie(Device device) {

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
		} else {
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

	@Data
	public class Command {
		private String in;// for incoming message
		private String out;// for outgoing message
		private int id;
		private CommandType type;
		private CommandState state;

		private static int idCounter = 0;// for unique id of command

		public Command() {
			this.id = idCounter++;
			if (idCounter == Integer.MAX_VALUE) {
				// Reset the counter if it exceeds the maximum value of an integer
				Command.idCounter = 0;
			}
			this.state = CommandState.NEW;
			this.in = "";
			this.out = "";
			this.type = null;
		}
		public String sendString() {
			return id + ";" + out;
		}
	}

	public enum CommandType {
		ADD_DEVICE,
		ADD_ROLLET,
		ADD_BUTTON,
		ADD_TERMOMETR,
		ADD_HIGROMETR,
		CHECK_INIT,
		CHECK_TO_WORK,
		REINIT,
		CHECK_STATE,
		CHECK_TEMPERATURE,
		CHECK_HIGROMETR,
		CHECK_RGB,
		CHECK_SWITCHES,
		CHECK_ROLLET,
		CHECK_BUTTONS,
		CHECK_DEVICES, CHANGEBLINDSTATE,
	}

	public enum CommandState {
		NEW,
		OK,
		ERROR,
		WAITING,
	}
}
