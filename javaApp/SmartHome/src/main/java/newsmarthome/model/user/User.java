package newsmarthome.model.user;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class User {
    protected Long id;
    protected String imie;
    protected String nazwisko;
    protected String nick;
    protected String email;
    protected String password;
    protected String oldPassword;
    protected String token;
    // Uprawnienia uprawnienia;
    // Opcje opcje;

    public User(){
        imie = "";
        nazwisko = "";
        nick = "";
        email = "";
        password = "";
        oldPassword = "";
        token = "";
    }

    
    public User(Long id, String imie, String nazwisko, String nick, String email, String password, String oldPassword, String token) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
        this.token = token;
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

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String toJSON(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
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
            e.printStackTrace();
        }
        return stream.toString();
    }

    @Override
    public String toString() {
        return this.toJSON();
    }

}
