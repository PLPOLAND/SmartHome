package smarthome.model;

/**
 * 
 * Klasa przetrzymująca dane o użytkowniku
 * 
 * @author Marek Pałdyna
 * @version 1.0
 */
public class User {
    Long id;
    String imie;
    String nazwisko;
    String nick;
    String email;
    String password;
    String oldPassword;
    Uprawnienia uprawnienia;

    public User(){

    }

    public User(Long id, String imie, String nazwisko, String nick, String email, String password, String oldPassword, Uprawnienia uprawnienia) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
        this.uprawnienia = uprawnienia;
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
    
}