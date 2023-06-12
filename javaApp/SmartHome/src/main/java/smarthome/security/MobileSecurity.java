package smarthome.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.database.UsersDAO;
import smarthome.model.Uprawnienia;
import smarthome.model.user.User;
import smarthome.model.user.Opcje;


/**
 * Klasa odpowiedzialna za logowanie użytkownika, sprawdzanie czy już się
 * zalogował, oraz pobierania danych zalogowanego użytkownika.
 * 
 * @author Marek Pałdyna
 * @version 1.0
 */
@Service
public class MobileSecurity {
    private static final String TOKEN_PARAMETER_NAME = "token";
    @Autowired
    UsersDAO database;
    HttpServletRequest request;

    Logger logger = LoggerFactory.getLogger(Security.class);//logger
    
    /**
     * Konstruktor Inicjuje Klasę do działania
     * 
     * @param req - typu HttpServletRequest - request ze strony
     * @param dat - typu UsersDAO - baza danych
     */
    public MobileSecurity(HttpServletRequest req, UsersDAO dat) {
        request = req;
        database = dat;
    }

    /**
     * Loguje użytkownika. Pobiera dane przesłane przez protokół POST Potrzebuje
     * conajmniej danej "login"
     * 
     * @version 1.0
     * @return token jeśli logowanie się powiodło lub "" jeśli nie
     */
    public String login() {
        String nickname;
        String pass;

        nickname = request.getParameter("nick").toString();
        pass = request.getParameter("pass").toString();

        if (nickname == null || nickname.isEmpty()) {
            return "";
        }

        User resultUsers = database.getUserLoginData(nickname, pass);
        if (resultUsers == null) {
            return "";
        } else {
            resultUsers.setToken(TokenGenerator.generateToken(nickname, resultUsers.getPassword()));
            database.update(resultUsers);
            return resultUsers.getToken();
        }
    }

    /**
     * 
     * Funkcja sprawdzająca czy użytkownik jest zalogowany
     * 
     * 
     * @version 1.0
     * @return true - jeśli uzytkownik jest zalogowany
     */
    public boolean isLoged() {
        String token = request.getParameter(TOKEN_PARAMETER_NAME);
        if (token == null || token.isEmpty()) {
            return false;
        }
        return database.getUserByToken(token) != null;

    }

    
    /**
     * Funkcja zwracająca uprawnienia użytkownika z danych sesji
     * 
     * @version 1.0
     * @return Uprawnienia użytkownika
     */
    public Uprawnienia getUserPremissions() {
        if (isLoged()) {
            String token = request.getParameter(TOKEN_PARAMETER_NAME);
            if (token == null || token.isEmpty()) {
                return null;
            }
            User result = database.getUserByToken(token);
            if (result == null) {
                return null;
            } else {
                return result.getUprawnienia();
            }
        } else {
            return null;
        }
    }

    /**
     * Funckja sprawdza czy zalogowany użytkownik jest Adminem
     * 
     * @return Czy użytkownik jest adminem
     */
    public boolean isUserAdmin() {
        String token = request.getParameter(TOKEN_PARAMETER_NAME);
        if (token == null || token.isEmpty()) {
            return false;
        }
        User result = database.getUserByToken(token);
        if (result == null) {
            return false;
        } else {
            return result.getUprawnienia().isAdmin();
        }
    }

    /**
     * Funkcja pobierająca dane użytkonika z bazy danych na podstawie ID 
     * (pobranego z danych sesji)
     * 
     * @return Dane zalogowanego użytkownika
     */
    public User getFullUserData() {
        String token = request.getParameter(TOKEN_PARAMETER_NAME);
        if (token == null || token.isEmpty()) {
            return null;
        }
        User result = database.getUserByToken(token);
        if (result == null) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Wylogowanie usera.
     * Usuwa dane usera z sesji.
     */
    public void logout() {
        String token = request.getParameter(TOKEN_PARAMETER_NAME);
        if (token == null || token.isEmpty()) {
            return;
        }
        User result = database.getUserByToken(token);
        if (result == null) {
            return;
        } else {
            result.setToken("");
            database.update(result);
        }
    }
}
