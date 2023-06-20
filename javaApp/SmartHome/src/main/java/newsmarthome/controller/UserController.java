package newsmarthome.controller;

import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import newsmarthome.database.SystemDAO;
import newsmarthome.database.UsersDAO;
import newsmarthome.security.MobileSecurity;
import newsmarthome.model.Room;
import newsmarthome.model.response.Response;
import newsmarthome.model.user.User;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	UsersDAO users;
	@Autowired
	SystemDAO systemDAO;

	Logger logger = LoggerFactory.getLogger(UserController.class);//logger



	@PostMapping("/login")
	public Response<TreeMap<String,String>> login(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		String token = security.login();
		if (token == null || token.isEmpty()) {
			return new Response<>(null, "Niepoprawne dane logowania");
		} else {
			logger.info("User {} logged in", request.getParameter("nick"));
			TreeMap<String,String> map = new TreeMap<>();
			map.put("token", token);
			return new Response<>(map);
		}
	}
	@PostMapping("/getUserData")
	public Response<String> getUserData(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");

		User user = security.getFullUserData();
		if (user == null) {
			return new Response<>(null, "Nie znaleziono użytkownika, błąd wewnętrzny!");
		} else {
			return new Response<>(user.toString());//TODO remove password from response
		}
	}

	@GetMapping("/getFavoriteRooms")
	public Response<String> getFavoriteRooms(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			User user = security.getFullUserData();
			return new Response<>(user.getFavoriteRooms());
		}
	}

	@PostMapping("/addFavoriteRoom")
	public Response<String> addFavoriteRoom(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			User user = security.getFullUserData();
			String roomID = request.getParameter("roomId");
			if(roomID == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					int roomIDInt = Integer.parseInt(roomID);
					Room room = systemDAO.getRoom(roomIDInt);
					if(room == null)
						return new Response<>(null, "Nie znaleziono pokoju o podanym ID");
					else{
						
						user.addFavoriteRoom(roomIDInt);
						logger.debug("User {} added room {} to favorites", user.getNick(), room.getName());
						return new Response<>("OK");
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole roomId musi być liczbą!");
				}
			}
		}
	}

	@PostMapping("/removeFavoriteRoom")
	public Response<String> removeFavoriteRoom(HttpServletRequest request) {
		MobileSecurity security = new MobileSecurity(request, users);
		if(!security.isLoged() )
			return new Response<>(null, "Użytkownik nie jest zalogowany");
		else{
			User user = security.getFullUserData();
			String roomID = request.getParameter("roomId");
			if(roomID == null)
				return new Response<>(null, "Nie przesłano wszystkich parametrów");
			else{
				try{
					int roomIDInt = Integer.parseInt(roomID);
					Room room = systemDAO.getRoom(roomIDInt);
					if(room == null)
						return new Response<>(null, "Nie znaleziono pokoju o podanym ID");
					else{
						user.removeFavoriteRoom(roomIDInt);
						logger.debug("User {} removed room {} from favorites", user.getNick(), room.getName());
						return new Response<>("OK");
					}
				}catch(NumberFormatException e){
					return new Response<>(null, "Pole roomId musi być liczbą!");
				}
			}
		}
	}
}
