package newsmarthome.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import newsmarthome.database.SystemDAO;
import newsmarthome.database.UsersDAO;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
// import newsmarthome.model.hardware.sensor.Higrometr; //TODO uncomment after adding higrometr
import newsmarthome.model.hardware.sensor.Sensor;
import newsmarthome.model.hardware.sensor.Termometr;
import newsmarthome.model.response.DeviceStateResponse;
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.response.SensorsStateResponse;
import newsmarthome.model.user.User;

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

}
