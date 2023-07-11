package newsmarthome.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.database.UsersDAO;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.sensor.Button;
// import newsmarthome.model.hardware.sensor.Higrometr; //TODO uncomment after adding higrometr
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.SensorsTypes;
import newsmarthome.model.hardware.sensor.Termometr;
import newsmarthome.model.response.DeviceStateResponse;
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.response.SensorsStateResponse;
import newsmarthome.model.user.User;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
public class SensorsController {

	@Autowired
	UsersDAO users;
	@Autowired
	SystemDAO systemDAO;

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
						//TODO: uncomment after adding higrometr
						// Higrometr higrometr = (Higrometr) sensor;
						// SensorsStateResponse<Float> sensorState2 = new SensorsStateResponse<>();
						// sensorState2.setId(sensor.getId());
						// sensorState2.addState(higrometr.getTemperatura());
						// sensorState2.addState((float)higrometr.getHumidity());
						// sensorsState.add(sensorState2);
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

	@PostMapping("./addSensor")
	public Response<Sensor> addSensor(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			String roomIDString = request.getParameter("room");
			String slaveID = request.getParameter("slaveID");
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			String automations = request.getParameter("automations");
			logger.debug("addSensor");
			logger.debug("room: {}",roomIDString);
			logger.debug("slaveID: {}",slaveID);
			logger.debug("name: {}",name);
			logger.debug("type: {}",type);
			if(roomIDString == null || slaveID == null || name == null || type == null)
				return new Response<>(null, "Nie podano wszystkich parametrów");
			else{
				try {
					int roomID = Integer.parseInt(roomIDString);
					int slaveIDint = Integer.parseInt(slaveID);
					Sensor sensor = null;
					SensorsTypes typeSensora = SensorsTypes.fromString(type);
					switch (typeSensora) {
						case THERMOMETR:
							return new Response<>(null, "Nie można dodać termometru ręcznie! Termometry dodawane są automatycznie po wykryciu na slave-ie.");
						case THERMOMETR_HYGROMETR:
							return new Response<Sensor>(null, "Not implemented yet");
						case TWILIGHT:
							return new Response<Sensor>(null, "Not implemented yet");
						case MOTION:
							return new Response<Sensor>(null, "Not implemented yet");
						case BUTTON:
							Room room = systemDAO.getRoom(roomID);
						 	Button button = systemDAO.addButton(room, name, slaveIDint, slaveIDint);
							if(automations != null){
								ObjectMapper mapper = new ObjectMapper();
								mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
								JsonNode jsonNode = mapper.readTree(automations);
								//TODO: load automations for button
							}
							return new Response<Sensor>(button);
						default:
							return new Response<Sensor>(null, "Nieznany typ czujnika");
					}
				}
				catch(IOException e){
					return new Response<>(null, "Błąd podczas parsowania automatyzacji");
				}
				catch(NumberFormatException e){
					return new Response<>(null, "");
				}
			}
		}

	}
	

}
