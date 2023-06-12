package smarthome.model.user;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
    String token;
    //TODO dodać czas ważności tokenu
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
        token = "";
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
        this.token = "";
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

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJSON(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        String json = "";
        OutputStream stream = new OutputStream() {
            private StringBuilder string = new StringBuilder();
            @Override
            public void write(int b) {
                this.string.append((char) b );
            }
            public String toString(){
                return this.string.toString();
            }
        };
        try {
            objectMapper.writeValue(stream, this);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stream.toString();
    }

    @Override
    public String toString() {
        return this.toJSON();
    }

}