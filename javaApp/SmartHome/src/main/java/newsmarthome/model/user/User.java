package newsmarthome.model.user;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import newsmarthome.database.UsersDAO;

@Component
@Scope("prototype")
public class User {
    protected Long id;
    protected String imie;
    protected String nazwisko;
    protected String nick;
    protected String email;
    protected String password;
    protected String oldPassword;
    protected String token;
    protected String favoriteRooms;
    // Uprawnienia uprawnienia;
    // Opcje opcje;

    private UsersDAO dao;

    public User(){
        imie = "";
        nazwisko = "";
        nick = "";
        email = "";
        password = "";
        oldPassword = "";
        token = "";
        favoriteRooms = "";
    }

    
    public User(Long id, String imie, String nazwisko, String nick, String email, String password, String oldPassword, String token, String favoriteRooms) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nick = nick;
        this.email = email;
        this.password = password;
        this.oldPassword = oldPassword;
        this.token = token;
        this.favoriteRooms = favoriteRooms;
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

    public void setFavoriteRooms(String favoriteRooms) {
        this.favoriteRooms = favoriteRooms;
        update();
    }

    public String getFavoriteRooms() {
        return this.favoriteRooms;
    }

    public void addFavoriteRoom(String roomName) {
        if (this.favoriteRooms == null || this.favoriteRooms.equals("")) {
            this.favoriteRooms = roomName;
        } else {
            String[] rooms = this.favoriteRooms.split(",");
            for (String room : rooms) {
                if (room==null || room.equals("")) {
                    continue;
                }
                if (room.equals(roomName)) {
                    return;
                }
            }
            this.favoriteRooms += "," + roomName;
        }
        update();
    }

    public void addFavoriteRoom(int roomID) {
        if (this.favoriteRooms == null || this.favoriteRooms.equals("")) {
            this.favoriteRooms = String.valueOf(roomID);
        } else {
            String[] rooms = this.favoriteRooms.split(",");
            for (String room : rooms) {
                if (room==null || room.equals("")) {
                    continue;
                }
                if (Integer.parseInt(room) == roomID) {
                    return;
                }
            }
            this.favoriteRooms += "," + roomID;
        }
        update();
    }
    
    public void removeFavoriteRoom(int roomName) {
        if (this.favoriteRooms != null) {
            String[] rooms = this.favoriteRooms.split(",");
            setFavoriteRooms("");
            for (String room : rooms) {
                if (Integer.parseInt(room) != roomName) {
                    this.addFavoriteRoom(Integer.parseInt(room));
                }
            }
        }
        update();
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

    private void update(){
        if (dao != null) {
            dao.update(this);
        }
    }

    public void setDao(UsersDAO dao) {
        this.dao = dao;
    }
}
