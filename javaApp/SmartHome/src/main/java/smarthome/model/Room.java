package smarthome.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Room {
    /** Logger Springa */
    Logger logger;

    int ID;//ID pokoju w systemie
    String nazwa;//nazwa pokoju
    
    List<Device> swiatla;// swiatla w pokoju
    List<Device> gniazdka;// gniazdka w pokoju
    List<Device> termometry;// termometry w pokoju

    public Room(){
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono nowy pokój:" + this.toString());
    }

    public Room(int ID, String nazwa) {
        this.ID = ID;
        this.nazwa = nazwa;
        this.swiatla = new ArrayList<>();
        this.gniazdka = new ArrayList<>();
        this.termometry = new ArrayList<>();
        logger = LoggerFactory.getLogger(Device.class);
        logger.info("Stworzono nowy pokój:" + this.toString());
    }

    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNazwa() {
        return this.nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public List<Device> getSwiatla() {
        return this.swiatla;
    }
    public List<Device> getGniazdka() {
        return this.gniazdka;
    }
    public List<Device> getTermometry() {
        return this.termometry;
    }
    public void addUrzadzenie(Device urz) throws Exception {
        if (urz.id < 0) {
            throw new Exception("Błędne ID urządzenia");
        }
        // if (urz.roomID < 0) { //TODO po za implementowaniu pojęcia płytki odkommentować
        //     throw new Exception("Błędny adres płytki");
        // }
        switch (urz.typ) {
            case GNIAZDKO:
                urz.room = this.ID;
                gniazdka.add(urz);
                break;
            case SWIATLO:
                urz.room = this.ID;
                swiatla.add(urz);
                break;
            case TERMOMETR:
                urz.room = this.ID;
                termometry.add(urz);
                break;
            default:
                throw new Exception("Nie obsługiwany typ urządzenia");
        }
        logger.info("Dodano urządzenie:" + urz.toString());
    }
    public void delUrzadzenie(Device urz) throws Exception {
        if (urz.room != this.ID) {
            throw new Exception("Podane urządzenie nie należy do tego pokoju");
        }
        switch (urz.typ) {
            case GNIAZDKO:
                gniazdka.remove(urz);
                break;
            case SWIATLO:
                swiatla.remove(urz);
            case TERMOMETR:
                termometry.remove(urz);
            default:
                throw new Exception("Nie obsługiwany typ urządzenia");
        }     
    }


    @Override
    public String toString() {
        return "{" +
            "ID='" + getID() + "'" +
            ", nazwa='" + getNazwa() + "'" +
            ", swiatla='" + getSwiatla() + "'" +
            ", gniazdka='" + getGniazdka() + "'" +
            ", termometry='" + getTermometry() + "'" +
            "}";
    }
    


}
