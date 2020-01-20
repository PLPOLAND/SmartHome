package smarthome.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.stereotype.Repository;

import smarthome.model.User;

/**
 * UsersDAO
 */
@Repository
public class UsersDAO {

	List<User> data = new ArrayList<User>();
	
	public UsersDAO(){
		this.readDatabase();
	}

	
	public List<User> getUserLoginData(String nickname, String pass) {
		return null;
	}

	public User find_user_by_id(Integer ID) {
		for (User user : data) {
			if (user.getId() == ID) {
				return user;// znaleziony user
			}
		}
		return null;// nie znaleziono usera
	}
	/**
	 * Czyta bazę danych z plików
	 */
	public void readDatabase() {
		ObjectMapper obj = new ObjectMapper();
		int i = 1;
		while (true) {
			User user = null;
			try {
				user = obj.readValue(
						TypeReference.class.getResourceAsStream("/static/database/users/" + i + "_User.json"),
						User.class);
				data.add(user);
				i++;
			} catch (Exception e) {
				// System.out.println("Wczytano " + --i + " userow");
				Logger logger = LoggerFactory.getLogger(UsersDAO.class);
				logger.info("Wczytano " + --i + " userow");
				break;
			}
		}
	}
	/**
	 * Pobieranie bazy danych Jeśli baza danych jest pusta pobiera ją a następnie
	 * zwraca
	 * 
	 * @return List<User> - baza danych
	 */
	public List<User> getDatabase() {
		if (data.isEmpty()) {
			this.readDatabase();
		}
		return this.data;
	}
    
	/**
	 * Zapisuje całą bazę danych do plików
	 */
	public void save() {
		for (User user : data) {
			this.save(user);
		}
	}

	/**
	 * Sprawdza czy baza danych zawiera już to ID
	 * 
	 * @param id - id do sprawdzenia
	 * @return boolean - czy baza zawiera dane ID
	 */
	public boolean contains(Long id) {
		boolean czyZawiera = false;
		for (User user : data) {
			if (user.getId() == id) {
				czyZawiera = true;
			}
		}
		return czyZawiera;
	}

	/**
	 * Funkcja sprawdzająca czy baza danych zawiera już taki nick
	 * 
	 * @param nick - nick do znalezienia
	 * @return true - jeśli znaleziono nick
	 */
	public boolean contains(String nick) {
		for (User user : data) {
			if (user.getNick() != null) {
				if (user.getNick().equals(nick)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Zwraca następne wole ID dla usera
	 * 
	 * @return Long - następne id
	 */
	public Long getNextID() {
		return new Long(data.size() + 1);
	}

	/**
	 * Zapisuje podany user do pliku
	 * 
	 * @param user - user do zapisania
	 */
	public boolean save(User user) {
		if (user != null) {

			try {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				File projFile = new File("src/main/resources/static/database/users/" + user.getId() + "_User.json");
				projFile.createNewFile();// utworzenie pliku jeśli nie istnieje
				objectMapper.writeValue(projFile, user);// plik projektu (src)
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				File appFile = new File(TypeReference.class.getResource("/static/database/users/").getPath()
						+ user.getId() + "_User.json");
				appFile.createNewFile();// utworzenie pliku jeśli nie istnieje
				objectMapper.writeValue(appFile, user);// plik aplikacji (target)
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			return false;
		}
	}

	
}