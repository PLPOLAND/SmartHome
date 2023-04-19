package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smarthome.database.UsersDAO;
import smarthome.security.Security;

@RestController
@RequestMapping("/api")
public class MobileAppController {

	@Autowired
	UsersDAO users;

	// @RequestMapping("/")
	// public String mainpage(HttpServletRequest request){
	// 	Security sec = new Security(request, users);
	// 	if(!sec.isLoged())
	// 		return "redirect:login";

	// 	return "index";
	// }
	@GetMapping("/hello")
	public String hello() {
		return "hello";
	}

}
