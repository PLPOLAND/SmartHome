package smarthome.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import smarthome.model.user.User;
import smarthome.security.Hash;

/**
 * UsersDAO
 */
@Repository
public class UsersDAO {

	private static final String USER_JSON = "_User.json";
	private static final String USERS_FILES_LOCATION = "smarthome/database/users/";
	List<User> userzy = new ArrayList<>();

	private Logger logger = LoggerFactory.getLogger(UsersDAO.class);

	public UsersDAO() {
		this.readDatabase();
	}

	public User getUserLoginData(String nickname, String pass) {
		User usr = null;
		for (User user : userzy) {
			if (user.getNick() != null) {
				if (user.getNick().equals(nickname)) {
					usr = user;
				}
			}

		}
		if (usr == null) {
			return null;
		} else {
			if (usr.getPassword().equals(Hash.hash(pass))) {
				return usr;
			} else {
				return null;
			}
		}
	}

	public User findUserById(Long id) {
		for (User user : userzy) {
			if (user.getId().longValue() == id.longValue()) {
				return user;// znaleziony user
			}
		}
		throw new IllegalArgumentException("Nie znaleziono uzytkownika o id: " + id);
	}

	/**
	 * Czyta bazę danych z plików
	 */
	public void readDatabase() {
		ObjectMapper obj = new ObjectMapper();
		int i = 0;

		try (Stream<Path> paths = Files.walk(Paths.get(USERS_FILES_LOCATION))) {
			paths.filter(Files::isRegularFile).forEach(filePath -> {
			User user = null;
				
				try {
					user = obj.readValue(
							new FileInputStream(new File(filePath.toString())),
							User.class);
				} catch (JsonParseException e) {
					logger.error("Błąd parsowania pliku JSON", e);
				} catch (JsonMappingException e) {
					logger.error("Błąd mapowania pliku JSON: {}, error: {}", filePath, e.getMessage());
				} catch (FileNotFoundException e) {
					logger.error("Nie znaleziono pliku: {}", filePath);
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("Błąd odczytu pliku: {}", filePath);
				}
				userzy.add(user);
			}
			);
		} catch (IOException e) {
			logger.info("Wczytano {} userow", userzy.size());
		}
	}
	
	/**
	 * Pobieranie bazy danych Jeśli baza danych jest pusta pobiera ją a następnie
	 * zwraca
	 * 
	 * @return List<User> - baza danych
	 */
	public List<User> getDatabase() {
		if (userzy.isEmpty()) {
			this.readDatabase();
		}
		return this.userzy;
	}

	public List<User> getUsers(){
		return getDatabase();
	}
	/**
	 * Sprawdza czy baza danych zawiera już to ID
	 * 
	 * @param id - id do sprawdzenia
	 * @return boolean - czy baza zawiera dane ID
	 */
	public boolean contains(Long id) {
		boolean czyZawiera = false;
		for (User user : userzy) {
			if (user.getId().equals(id)) {
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
		for (User user : userzy) {
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
	public long getNextID() {
		return userzy.size() + 1L;
	}
	/**
	 * Tworzy nowego usera
	 * 
	 * @param user - user do stworzenia
	 */
	public void createUser(User user) {
		user.setPassword(Hash.hash(user.getPassword()));//zahaszuj hasło
		this.save(user);
		this.userzy.add(user);
	}
	/**
	 * Zapisuje całą bazę danych do plików
	 */
	public void save() {
		for (User user : userzy) {
			this.save(user);
		}
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
				File projFile = new File(USERS_FILES_LOCATION + user.getId() + USER_JSON);
				new File(USERS_FILES_LOCATION).mkdirs();
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
				File appFile = new File(TypeReference.class.getResource("/static/database/users/").getPath() + user.getId() + USER_JSON);
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