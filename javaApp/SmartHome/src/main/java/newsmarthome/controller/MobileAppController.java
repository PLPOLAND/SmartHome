package newsmarthome.controller;

import java.util.ArrayList;
import java.util.TreeMap;

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
import newsmarthome.i2c.MasterToSlaveConverter;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Room;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.response.Response;
import newsmarthome.model.response.RoomResponse;
import newsmarthome.model.user.User;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class MobileAppController {

	@Autowired
	UsersDAO users;
	@Autowired
	SystemDAO systemDAO;
	@Autowired
	MasterToSlaveConverter masterToSlaveConverter;


	Logger logger = LoggerFactory.getLogger(MobileAppController.class);//logger

	/** 
	 * For serching server from mobile app
	 */
	@GetMapping("/homeData")
	public String hello() {
		return "hello";
	}

	@PostMapping("/restartSlaves")
	public String restartSlaves() {
		masterToSlaveConverter.restartAllSlaves();
		return "ok";
	}
	

}
