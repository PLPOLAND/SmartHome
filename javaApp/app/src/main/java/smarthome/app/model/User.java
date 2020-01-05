package smarthome.app.model;

/**
 * User 
 * 
 * Klasa przetrzymująca dane o użytkowniku
 * 
 * @author Marek Pałdyna
 */
public class User {
    int id;
    String imie;
    String nazwisko;
    String nick;
    String email;
    String password;
    String oldPassword;

    public User(int id, String imie, String nazwisko, String nick, String email, String password, String oldPassword) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
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
    
}