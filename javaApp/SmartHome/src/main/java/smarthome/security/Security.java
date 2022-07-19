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
public class Security {
    @Autowired
    UsersDAO database;
    HttpServletRequest request;

    Logger logger = LoggerFactory.getLogger(Security.class);//logger
    
    /**
     * Konstruktor Inicjuje Klasę do działania
     * 
     * @param req - typu HttpServletRequest - request ze strony
     * @param dat - typu UsersDAO - instancja klasy do połączeń z bazą danych
     */
    public Security(HttpServletRequest req, UsersDAO dat) {
        request = req;
        database = dat;
    }

    /**
     * Loguje użytkownika. Pobiera dane przesłane przez protokół POST Potrzebuje
     * conajmniej danej "login"
     * 
     * @version 1.0
     * @return true jeśli logowanie się powiodło
     */
    public boolean login() {
        String nickname;
        String pass;

        nickname = request.getParameter("nick").toString();
        pass = request.getParameter("pass").toString();

        if (nickname == null || nickname.isEmpty()) {
            return false;
        }

        User resultUsers = database.getUserLoginData(nickname, pass);
        if (resultUsers == null) {
            return false;
        } else {
            String name = resultUsers.getImie();
            String nazwisko = resultUsers.getEmail();
            Long idU = resultUsers.getId();
            HttpSession session = request.getSession();
            session.setAttribute("imie", name); // dodawanie pola do sesji
            session.setAttribute("nazwisko", nazwisko);
            session.setAttribute("id", idU);
            session.setAttribute("uprawnienia",resultUsers.getUprawnienia());
            session.setAttribute("opcje",resultUsers.getOpcje());
            session.setMaxInactiveInterval(60 * 60 * 24 * 2); // usuniecie pol sesji po 2 dniach nie aktywności

            logger.info("User: " + resultUsers.getNick() + " zalogował się pomyślnie"); //log

            return true;
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
        HttpSession session = request.getSession();
        if (session.getAttribute("imie") == null || session.getAttribute("nazwisko") == null
                || session.getAttribute("id") == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Funkcja zwracająca Imię użytkownika pobieraną z danych sesji
     * 
     * @version 1.0
     * @return Nazwe użytkownika
     */
    public String getUserName() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            return session.getAttribute("imie").toString();
        } else
            return null;
    }

    /**
     * Funkcja zwracająca ID użytkonika pobierany z danych sesji
     * 
     * @version 1.0
     * @return Integer ID użytkownika
     */
    public Integer getUserID() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            return (Integer) session.getAttribute("id");
        } else {
            return null;
        }
    }

    /**
     * Funckja zwracająca Nazwisko użytkownika z danych sesji *
     * 
     * @version 1.0
     * @return Nazwisko
     */
    public String getUserSurName() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            return session.getAttribute("Nazwisko").toString();
        } else {
            return null;
        }
    }

    /**
     * Funkcja zwracająca uprawnienia użytkownika z danych sesji
     * 
     * @version 1.0
     * @return Typ Użytkownika
     */
    public Uprawnienia getUserPremissions() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            return (Uprawnienia) session.getAttribute("uprawnienia");
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
        if (isLoged()) {
            if (this.getUserPremissions().isAdmin()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Funkcja pobierająca dane użytkonika z bazy danych na podstawie ID 
     * (pobranego z danych sesji)
     * 
     * @return Dane zalogowanego użytkownika
     */
    public User getFullUserData() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            User result = database.findUserById((Long) session.getAttribute("id"));
            if (result == null) {
                return null;
            } else {
                return result;
            }
        } else {
            return null;
        }
    }
    public String getUserAvatarPath(){
        if (isLoged()) {
            HttpSession session = request.getSession();
            return ((Opcje)session.getAttribute("opcje")).getLokalnaSciezka();//TODO dodać Gravatar
        } else {
            return null;
        }
    }

    public String getUserThemePath() {
        if (isLoged()) {
            HttpSession session = request.getSession();
            return ((Opcje) session.getAttribute("opcje")).getThemeSciezka();// TODO dodać Gravatar
        } else {
            return null;
        }
    }

    /**
     * Wylogowanie usera.
     * Usuwa dane usera z sesji.
     */
    public void logout() {
        if (isLoged()) {
            HttpSession session = request.getSession();

            session.removeAttribute("imie"); // usuwanie pola do sesji
            session.removeAttribute("nazwisko");
            session.removeAttribute("id");
            session.removeAttribute("uprawnienia");
            session.removeAttribute("opcje");
        } else
            return;
    }

}