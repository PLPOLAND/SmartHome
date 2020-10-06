package smarthome.database;

import org.springframework.stereotype.Repository;

import smarthome.model.Device;
import smarthome.model.Room;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class SystemDAO {

    TreeMap<String,Room> pokoje; //Pokoje w systemie


    public SystemDAO() {
        Logger logger = LoggerFactory.getLogger(UsersDAO.class);
        logger.info("Init System DAO");
        pokoje = new TreeMap<>();
        this.readDatabase();
    }
    /**
     * Dodaje pokój do systemu
     * @param pokoj - pokój do dodania
     */
    public void addRoom(Room pokoj){
        this.pokoje.put(pokoj.getNazwa(), pokoj);
        save(pokoj);
    }
    
    /**
     * Zwraca pokoje w systemie w postaci ArrayList
     * 
     * @return ArrayList<Room>
     */
    public ArrayList<Room> getRoomsArrayList(){
        return new ArrayList<Room>(pokoje.values());
    }
    /**
     * Dodaje urządzenie do wskazanego pokoju
     * @param name - klucz pokoju / nazwa w systemie
     * @param d - urządzenie do dodania
     * @throws Exception
     */
    public void addDeviceToRoom(String name, Device d) throws Exception{
        this.pokoje.get(name).addUrzadzenie(d);
        save(this.pokoje.get(name));
    }


    /**
     * Czyta bazę danych z plików
     */
    public void readDatabase() {
        ObjectMapper obj = new ObjectMapper();
        int i = 0;
        while (true) {
            Room room = null;
            try {
                room = obj.readValue(new FileInputStream(new File("src/main/resources/static/database/rooms/" + i + "_Room.json")),
                        Room.class);
                pokoje.put(room.getNazwa(),room);
                i++;
            } catch (Exception e) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.info("Wczytano " + i + " pokoi");
                break;
            }
        }
    }


    /**
     * Zapisuje całą bazę danych do plików
     */
    public void save() {
        for (Room pokoj : this.getRoomsArrayList()) {
            this.save(pokoj);
        }
    }

    /**
     * Zapisuje podany pokoj do pliku
     * 
     * @param pokoj - pokoj do zapisania
     */
    public boolean save(Room pokoj) {
        if (pokoj != null) {

            try {//tworzenie plików w plikach src
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                File projFile = new File("src/main/resources/static/database/rooms/" + pokoj.getID() + "_Room.json");
                projFile.getParentFile().mkdirs();
                projFile.createNewFile();// utworzenie pliku jeśli nie istnieje
                objectMapper.writeValue(projFile, pokoj);// plik projektu (src)
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO Czy potrzebne???
            // try {//towrzenie plików w plikach programu

            //     ObjectMapper objectMapper = new ObjectMapper();
            //     objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            //     File appFile = new File(TypeReference.class.getResource("/static/database/rooms/").getPath()
            //             + pokoj.getID() + "_Room.json");
                        
            //     appFile.getParentFile().mkdirs();
            //     appFile.createNewFile();// utworzenie pliku jeśli nie istnieje
            //     objectMapper.writeValue(appFile, pokoj);// plik aplikacji (target)
            // } catch (JsonGenerationException e) {
            //     e.printStackTrace();
            // } catch (JsonMappingException e) {
            //     e.printStackTrace();
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
            return true;
        } else {
            return false;
        }
    }
}