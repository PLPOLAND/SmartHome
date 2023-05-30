package newsmarthome.controller;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import newsmarthome.database.UsersDAO;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Response;
import newsmarthome.model.user.User;

@RestController
@RequestMapping("/api")
public class MobileAppController {

	@Autowired
	UsersDAO users;

	Logger logger = LoggerFactory.getLogger(MobileAppController.class);//logger

	/** 
	 * For serching server from mobile app
	 */
	@GetMapping("/homeData")
	public String hello() {
		return "hello";
	}

	@PostMapping("/login")
	public Response<String> login(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		String token = security.login();
		if (token == null || token.isEmpty()) {
			return new Response<>("", "Niepoprawne dane logowania");
		} else {
			return new Response<>("{\"token\": \""+ token + "\"}");
		}
	}
	@PostMapping("/getUserData")
	public Response<String> getUserData(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>("", "Użytkownik nie jest zalogowany");

		User user = security.getFullUserData();
		if (user == null) {
			return new Response<>("", "Nie znaleziono użytkownika, błąd wewnętrzny!");
		} else {
			return new Response<>(user.toString());//TODO remove password from response
		}
	}

}
