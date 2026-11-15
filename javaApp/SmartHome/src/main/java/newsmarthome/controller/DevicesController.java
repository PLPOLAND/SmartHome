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
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.response.DeviceStateResponse;
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.user.User;
import newsmarthome.service.ConfigurationException;
import newsmarthome.service.ConfigurationService;

@RestController
@RequestMapping("/api")
public class DevicesController {

	final UsersDAO users;
	final SystemDAO systemDAO;

	final ConfigurationService configurationService;

	Logger logger = LoggerFactory.getLogger(DevicesController.class);


    DevicesController(@Autowired UsersDAO users, @Autowired SystemDAO systemDAO,
            @Autowired ConfigurationService configurationService) {
        this.users = users;
        this.systemDAO = systemDAO;
        this.configurationService = configurationService;
    }

	@GetMapping("/getDevices")
	public Response<ArrayList<Device>> getDevices(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			return new Response<>(systemDAO.getDevices());
		}
	}
	
	@GetMapping("/getDevice")
	public Response<Device> getDevice(HttpServletRequest request){
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			String deviceID = request.getParameter("deviceId");
			logger.debug("getDevice");
			logger.debug("deviceID: {}",deviceID);
			if(deviceID == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					int devID = Integer.parseInt(deviceID);
					Device dev = systemDAO.getDeviceByID(devID);
					if(dev == null)
						return new Response<>(null, "Nie znaleziono urządzenia o podanym ID");
					else{
						return new Response<>(dev);
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			}
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
			logger.debug("deviceID: {}",deviceID);
			logger.debug("state: {}", state);
			if(deviceID == null || state == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					DeviceState deviceState = DeviceState.fromString(state);
					int devID = Integer.parseInt(deviceID);
					Device device = systemDAO.getDeviceByID(devID);
					if(device == null)
						return new Response<>(null, "Nie znaleziono urządzenia o podanym ID");
					else{
						device.changeState(deviceState);
						return new Response<>("OK");
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			}
		}
	}
	
	@GetMapping("/getDevicesState")
	public Response<ArrayList<DeviceStateResponse>> getDevicesState(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			return new Response<>(systemDAO.getDevices().stream().map(DeviceStateResponse::new).collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
		}
	}

	@PostMapping("/addDevice")
	public Response<Device> addDevice(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		String roomID = request.getParameter("roomID");
		String type = request.getParameter("type");
		String slaveID = request.getParameter("slaveID");
		String name = request.getParameter("name");
		String devicePin = request.getParameter("pin");
		String pin2 = request.getParameter("pin2");
		logger.debug("addDevice roomID: {}, type: {}, slaveID: {}, name: {}, pin: {}, pin2: {}", roomID, type, slaveID, name, devicePin, pin2);
		if (roomID == null || type == null || slaveID == null || devicePin == null)
			return new Response<>(null, "Nie przesłano wszystkich parametrów");
		try{
			return new Response<>(configurationService.addDevice(name, DeviceTypes.fromString(type), Integer.parseInt(roomID),
					Integer.parseInt(slaveID), Integer.parseInt(devicePin), pin2 != null ? Integer.valueOf(pin2) : null));
		}catch(NumberFormatException e){
			return new Response<>(null, "Pole roomID|devicePin|slaveID|pin2 musi być liczbą!");
		}catch(ConfigurationException e){
			return new Response<>(null, e.getMessage());
		}
	}

	@PostMapping("/removeDevice")
	public Response<String> removeDevice(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		String deviceID = request.getParameter("deviceId");
		logger.debug("removeDevice deviceID: {}",deviceID);
		if(deviceID == null)
			return new Response<>(null, "Nie przesłano wszystkich parametrów");
		try{
			configurationService.removeDevice(Integer.parseInt(deviceID));
			return new Response<>("OK");
		}catch(NumberFormatException e){
			return new Response<>(null, "Pole deviceId musi być liczbą!");
		}catch(ConfigurationException e){
			return new Response<>(null, e.getMessage());
		}
	}

	/** Zmienia przesłane pola urządzenia: name, pin, pin2 (roleta), room, slaveId. */
	@PostMapping("/updateDevice")
	public Response<String> updateDevice(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		String deviceID = request.getParameter("deviceId");
		String name = request.getParameter("name");
		String pin = request.getParameter("pin");
		String pin2 = request.getParameter("pin2");
		String room = request.getParameter("room");
		String slaveId = request.getParameter("slaveId");
		logger.debug("updateDevice deviceID: {}, name: {}, pin: {}, pin2: {}, room: {}, slaveId: {}", deviceID, name, pin, pin2, room, slaveId);
		if(deviceID == null)
			return new Response<>(null, "Nie przesłano ID urządzenia");
		if (name == null && pin == null && pin2 == null && room == null && slaveId == null)
			return new Response<>(null, "Nie przesłano żadnych parametrów");
		try{
			configurationService.updateDevice(Integer.parseInt(deviceID), name, toInteger(room), toInteger(slaveId),
					toInteger(pin), toInteger(pin2));
			return new Response<>("OK");
		}catch(NumberFormatException e){
			return new Response<>(null, "Pola deviceId, pin, pin2, room i slaveId muszą być liczbami!");
		}catch(ConfigurationException e){
			return new Response<>(null, e.getMessage());
		}
	}

	private static Integer toInteger(String value) {
		return value != null ? Integer.valueOf(value) : null;
	}

}
