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
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceState;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.device.Outlet;
import newsmarthome.model.hardware.device.Fan;
import newsmarthome.model.response.DeviceStateResponse;
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

	@Autowired
	HardwareFactory hardwareFactory;

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
		else{
			String roomID = request.getParameter("roomID");
			String type = request.getParameter("type");
			String slaveID = request.getParameter("slaveID");
			String name = request.getParameter("name");
			String devicePin = request.getParameter("pin");
			String pin2 = request.getParameter("pin2");
			logger.debug("addDevice");
			logger.debug("roomID: {}",roomID);
			logger.debug("type: {}", type);
			logger.debug("slaveID: {}", slaveID);
			logger.debug("name: {}", name);
			logger.debug("devicePin: {}", devicePin);

			Room room = systemDAO.getRoom(Integer.parseInt(roomID));
			if(room == null)
				return new Response<>(null, "Nie znaleziono pokoju o podanym ID");
			else{
				try{
					int pin = Integer.parseInt(devicePin);
					int slave = Integer.parseInt(slaveID);
					Integer pin2Integer = null;
					if(pin2 != null)
						pin2Integer = Integer.parseInt(pin2);
					DeviceTypes deviceTypes = DeviceTypes.fromString(type);
					Device dev = systemDAO.addDevice(room, name, deviceTypes, slave,pin, pin2Integer);
					if(dev == null)
						return new Response<>(null, "Nie udało się dodać urządzenia");
					else{
						return new Response<>(dev);
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			}
			
		}
	}

	@PostMapping("/removeDevice")
	public Response<String> removeDevice(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			String deviceID = request.getParameter("deviceId");
			logger.debug("removeDevice");
			logger.debug("deviceID: {}",deviceID);
			if(deviceID == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					int devID = Integer.parseInt(deviceID);
					if (systemDAO.removeDevice(devID))
						return new Response<>("OK");
					else
						return new Response<>(null, "Nie udało się usunąć urządzenia");
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			}
		}
	}

	@PostMapping("updateDevice")
	public Response<String> updateDevice(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			String deviceID = request.getParameter("deviceId");
			String name = request.getParameter("name");
			String pin = request.getParameter("pin");
			String pin2 = request.getParameter("pin2");
			String room = request.getParameter("room");
			logger.debug("updateDevice");
			logger.debug("deviceID: {}",deviceID);
			logger.debug("name: {}", name);
			logger.debug("pin: {}", pin);
			logger.debug("pin2: {}", pin2);
			logger.debug("room: {}", room);
			if(deviceID == null){
				return new Response<>(null, "Nie przesłano ID urządzenia");
			}
			if (name != null){
				try{
					int devID = Integer.parseInt(deviceID);
					Device dev = systemDAO.getDeviceByID(devID);
					if (dev !=null) {
						dev.setName(name);
						return new Response<>("OK");
					} else {
						return new Response<>(null, "Nie udało się zmienić nazwy urządzenia: nie znaleziono urządzenia o podanym ID");
					}
				} catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			}
			if( pin != null){
				try{
					int devID = Integer.parseInt(deviceID);
					Device dev = systemDAO.getDeviceByID(devID);
					if (!(dev instanceof Blind)) {
						switch (dev.getTyp()) {
							case LIGHT:
								((Light)dev).setPin(Integer.parseInt(pin));
								return new Response<>("OK");
							case GNIAZDKO:
								((Outlet)dev).setPin(Integer.parseInt(pin));
								return new Response<>("OK");
							case WENTYLATOR:
								((Fan)dev).setPin(Integer.parseInt(pin));
								return new Response<>("OK");
							default:
								logger.error ("Nie udało się zmienić pinu urządzenia: nieznany typ urządzenia");
								return new Response<>(null, "Nie udało się zmienić pinu urządzenia: nieznany typ urządzenia");
						}
					} else{
						if (pin2 != null) {
							((Blind)dev).setPinUp(Integer.parseInt(pin));
							((Blind)dev).setPinDown(Integer.parseInt(pin2));
							return new Response<>("OK");
						} else {
							return new Response<>(null, "Nie udało się zmienić pinu urządzenia: nie podano pinu2");
						}
					}
				} catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId || pin || pin2 muszą być liczbami!");
				}
			}
			if (room !=null){
				try{
					int devID = Integer.parseInt(deviceID);
					Device dev = systemDAO.getDeviceByID(devID);
					if (dev !=null) {
						Room newRoom = systemDAO.getRoom(Integer.parseInt(room));
						if (newRoom != null) {
							Room oldRoom = systemDAO.getRoom(dev.getRoom());
							oldRoom.delDevice(dev);
							newRoom.addDevice(dev);
							return new Response<>("OK");
						} else {
							return new Response<>(null, "Nie udało się zmienić pokoju urządzenia: nie znaleziono pokoju o podanym ID");
						}
					} else {
						return new Response<>(null, "Nie udało się zmienić pokoju urządzenia: nie znaleziono urządzenia o podanym ID");
					}
				} catch(NumberFormatException e){
					return new Response<>(null, "Pole deviceId musi być liczbą!");
				}
			
			}
			else{
				return new Response<>(null, "Nie przesłano żadnych parametrów");
			}
		}
	}

	
}
