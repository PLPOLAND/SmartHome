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
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.user.User;

@RestController
@RequestMapping("/api")
public class DevicesController {

	@Autowired
	UsersDAO users;
	@Autowired
	SystemDAO systemDAO;

	Logger logger = LoggerFactory.getLogger(DevicesController.class);//logger

	@GetMapping("/getDevices")
	public Response<ArrayList<Device>> getDevices(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			return new Response<>(systemDAO.getDevices());
		}
	}
	
	@GetMapping("/getRooms")
	public Response<ArrayList<RoomResponse>> getRooms(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			return new Response<>(systemDAO.getRoomsArrayList().stream().map(RoomResponse::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
		}
	}


	@PostMapping("/changeDeviceState")
	public Response<String> changeDeviceState(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			String deviceID = request.getParameter("deviceId");
			String state = request.getParameter("state");
			DeviceState deviceState = DeviceState.fromString(state);
			logger.info("deviceID: {}",deviceID);
			logger.info("state: {}", deviceState.name());
			if(deviceID == null || state == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					int devID = Integer.parseInt(deviceID);
					Device device = systemDAO.getDeviceByID(devID);
					if(device == null)
						return new Response<>(null, "Nie znaleziono urządzenia o podanym ID");
					else{
						device.changeState(deviceState);
						return new Response<>("OK");
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceID musi być liczbą!");
				}
			}
		}
	}

}
