package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


import smarthome.database.UsersDAO;
import smarthome.security.Security;

@Controller
public class MainController {

	@Autowired
	UsersDAO users;

	@RequestMapping("/")
	public String mainpage(){
		return "mainpage";
	}

	@RequestMapping("/login")
	public String loadLoginPage() {
		return "loginPage";
	}

	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) {
		Security sec = new Security(request, users);
		sec.logout();
		return "redirect:/";
	}

}
