package smarthome.model.user;

import org.springframework.stereotype.Component;
import smarthome.model.Uprawnienia;

/**
 * 
 * Klasa przetrzymująca dane o użytkowniku
 * 
 * @author Marek Pałdyna
 * @version 1.0
 */
@Component
public class User {
    Long id;
    String imie;
    String nazwisko;
    String nick;
    String email;
    String password;
    String oldPassword;
    Uprawnienia uprawnienia;
    Opcje opcje;

    public User(){
        opcje = new Opcje();
        uprawnienia = new Uprawnienia(false);
        imie = "";
        nazwisko = "";
        nick = "";
        email = "";
        password = "";
        oldPassword = "";
    }

    /**
     * 
     * @param id
     * @param imie
     * @param nazwisko
     * @param nick
     * @param email
     * @param password
     * @param oldPassword
     * @param uprawnienia
     * @param opcje
     */
    public User(Long id, String imie, String nazwisko, String nick, String email, String password, String oldPassword, Uprawnienia uprawnienia, Opcje opcje) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
        this.uprawnienia = uprawnienia;
        this.opcje = opcje;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImie() {
        return this.imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getNazwisko() {
        return this.nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    public String getNick() {
        return this.nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return this.oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Uprawnienia getUprawnienia() {
        return this.uprawnienia;
    }

    public void setUprawnienia(Uprawnienia uprawnienia) {
        this.uprawnienia = uprawnienia;
    }

    public Opcje getOpcje() {
        return this.opcje;
    }

    public void setOpcje(Opcje opcje) {
        this.opcje = opcje;
    }

    
}