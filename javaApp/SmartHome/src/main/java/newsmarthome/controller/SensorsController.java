package newsmarthome.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.database.UsersDAO;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.ButtonLocalFunction;
import newsmarthome.model.hardware.sensor.Higrometr;
// import newsmarthome.model.hardware.sensor.Higrometr; //TODO uncomment after adding higrometr
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;
import newsmarthome.model.response.DeviceStateResponse;
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.response.SensorsStateResponse;
import newsmarthome.model.user.User;
import newsmarthome.service.ClickFunctionSpec;
import newsmarthome.service.ConfigurationException;
import newsmarthome.service.ConfigurationService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
public class SensorsController {

	@Autowired
	UsersDAO users;
	@Autowired
	SystemDAO systemDAO;
	@Autowired
	ConfigurationService configurationService;

	Logger logger = LoggerFactory.getLogger(SensorsController.class);//logger

	@GetMapping("/getSensors")
	public Response<ArrayList<Sensor>> getSensors(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			return new Response<>(systemDAO.getSensors());
		}
	}


	
	@GetMapping("/getSensorsState")
	public Response<ArrayList<SensorsStateResponse>> getSensorsState(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			ArrayList<SensorsStateResponse> sensorsState = new ArrayList<>();
			for(Sensor sensor : systemDAO.getSensors()){
				switch (sensor.getTyp()) {
					case THERMOMETR:
						Termometr termometr = (Termometr) sensor;
						SensorsStateResponse<Float> sensorState = new SensorsStateResponse<>();
						sensorState.setId(sensor.getId());
						sensorState.addState(termometr.getTemperatura());
						sensorsState.add(sensorState);
						break;
					case THERMOMETR_HYGROMETR:
						//TODO: check if it works
						Higrometr higrometr = (Higrometr) sensor;
						SensorsStateResponse<Float> sensorState2 = new SensorsStateResponse<>();
						sensorState2.setId(sensor.getId());
						sensorState2.addState(higrometr.getTemperatura());
						sensorState2.addState((float)higrometr.getHumidity());
						sensorsState.add(sensorState2);
						break;
					case TWILIGHT:
						//TODO dodać obsługę po dodaniu czujnika zmierzchu
						break;
					case MOTION:
						//TODO dodać obsługę po dodaniu czujnika ruchu
						break;
					default:
						break;
				}
			}
			return new Response<>(sensorsState);
			// return new Response<>(systemDAO.getSensors().stream().map(SensorsStateResponse::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
		}
	}

	@GetMapping("/getSensor")
	public Response<Sensor> getSensor(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			try {
				if (request.getParameter("id") != null) {
					int id = Integer.parseInt(request.getParameter("id"));
					logger.debug("getSensor");
					logger.debug("id: {}",id);
					return new Response<>(systemDAO.getSensor(id));
				}
				else
					return new Response<>(null, "Nie podano ID");
			} catch (NumberFormatException e) {
				return new Response<>(null, "ID nie jest liczbą");
			}
		}
	}

	@PostMapping("/addSensor")
	public Response<Sensor> addSensor(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		String roomIDString = request.getParameter("roomID");
		String slaveID = request.getParameter("slaveID");
		String name = request.getParameter("name");
		String type = request.getParameter("type");
		String pin = request.getParameter("pin");
		String automations = request.getParameter("funkcjeKlikniec");
		logger.debug("addSensor room: {}, slaveID: {}, name: {}, type: {}", roomIDString, slaveID, name, type);
		if(roomIDString == null || slaveID == null || name == null || type == null)
			return new Response<>(null, "Nie podano wszystkich parametrów");
		try {
			SensorsTypes sensorType = SensorsTypes.fromString(type);
			if (sensorType == SensorsTypes.TWILIGHT || sensorType == SensorsTypes.MOTION)
				return new Response<>(null, "Not implemented yet");
			if (sensorType == SensorsTypes.NONE)
				return new Response<>(null, "Nieznany typ czujnika");
			List<ClickFunctionSpec> clickFunctions = null;
			if (automations != null && sensorType == SensorsTypes.BUTTON) {
				clickFunctions = parseClickFunctions(automations);
				if (clickFunctions == null)
					return new Response<>(null, INVALID_CLICK_FUNCTION);
			}
			Sensor sensor = configurationService.addSensor(sensorType, name, Integer.parseInt(roomIDString),
					Integer.parseInt(slaveID), pin != null ? Integer.valueOf(pin) : null, clickFunctions);
			logger.info("{}", sensor);
			return new Response<>(sensor);
		}
		catch(IOException e){
			return new Response<>(null, "Błąd podczas parsowania automatyzacji");
		}
		catch(NumberFormatException e){
			return new Response<>(null, "Parametr liczbowy nie jest liczbą");
		}
		catch(ConfigurationException e){
			return new Response<>(null, e.getMessage());
		}
	}

	@PostMapping("/removeSensor")
	Response<Boolean> removeSensor(HttpServletRequest request){
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(false, "Użytkownik nie jest zalogowany");
		String idString = request.getParameter("id");
		if(idString == null)
			return new Response<>(false, "Nie podano ID");
		try {
			configurationService.removeSensor(Integer.parseInt(idString));
			return new Response<>(true);
		}
		catch(NumberFormatException e){
			return new Response<>(false, "ID nie jest liczbą");
		}
		catch(ConfigurationException e){
			return new Response<>(false, e.getMessage());
		}
	}

	@PostMapping("/updateSensor")
	Response<Sensor> updateSensor(HttpServletRequest request){
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		String idString = request.getParameter("id");
		String name = request.getParameter("name");
		String slaveID = request.getParameter("slaveID");
		String pin = request.getParameter("pin");
		String automations = request.getParameter("funkcjeKlikniec");
		String roomIDString = request.getParameter("roomID");
		if(idString == null)
			return new Response<>(null, "Nie podano ID");
		try {
			List<ClickFunctionSpec> clickFunctions = null;
			if (automations != null) {
				clickFunctions = parseClickFunctions(automations);
				if (clickFunctions == null)
					return new Response<>(null, INVALID_CLICK_FUNCTION);
			}
			return new Response<>(configurationService.updateSensor(Integer.parseInt(idString), name,
					roomIDString != null ? Integer.valueOf(roomIDString) : null,
					slaveID != null ? Integer.valueOf(slaveID) : null, pin != null ? Integer.valueOf(pin) : null,
					clickFunctions));
		}
		catch(IOException e){
			return new Response<>(null, "Błąd podczas parsowania automatyzacji");
		}
		catch(NumberFormatException e){
			return new Response<>(null, "id, roomID, slaveID i pin muszą być liczbami");
		}
		catch(ConfigurationException e){
			return new Response<>(null, e.getMessage());
		}
	}


	private static final String INVALID_CLICK_FUNCTION = "Niepoprawna funkcja kliknięcia: wymagane liczbowe clicks, znany state i liczbowe device";
	/**
	 * Wartości state, które wysyłają klienci: stany funkcji (NONE/UP/DOWN/STOP) i stany urządzeń z
	 * aplikacji mobilnej (DeviceState.toString(): none/on/off/up/down/middle/run, NOTKNOW z serwera).
	 * Inne (np. literówki) odrzucamy - State.fromString zamieniłby je po cichu na NONE.
	 */
	private static final Set<String> KNOWN_CLICK_STATES = new HashSet<>(
			Arrays.asList("NONE", "UP", "DOWN", "STOP", "ON", "OFF", "MIDDLE", "NOTKNOW", "RUN"));

	/**
	 * Parsuje funkcje kliknięć przycisku z parametru funkcjeKlikniec (lista JSON).
	 * @return funkcje albo null, gdy któraś nie ma liczbowych clicks i device lub znanego tekstowego state
	 *         (istnienie urządzenia sprawdza ConfigurationService)
	 */
	private List<ClickFunctionSpec> parseClickFunctions(String automations) throws IOException {
		JsonNode list = new ObjectMapper().readTree(automations);
		if (list == null || !list.isArray())
			return null;
		List<ClickFunctionSpec> functions = new ArrayList<>();
		for (JsonNode json : list) {
			if (!isNonNegativeInt(json.path("clicks")) || !isNonNegativeInt(json.path("device"))
					|| !json.path("state").isTextual()
					|| !KNOWN_CLICK_STATES.contains(json.get("state").asText().toUpperCase()))
				return null;
			functions.add(new ClickFunctionSpec(json.get("clicks").asInt(), json.get("device").asInt(),
					ButtonLocalFunction.State.fromString(json.get("state").asText())));
		}
		return functions;
	}

	/** Nieujemna liczba całkowita, także zapisana jako tekst - tak jak dotąd przyjmowało asInt(). */
	private static boolean isNonNegativeInt(JsonNode node) {
		return (node.isInt() && node.asInt() >= 0) || (node.isTextual() && node.asText().matches("\\d{1,9}"));
	}

}
