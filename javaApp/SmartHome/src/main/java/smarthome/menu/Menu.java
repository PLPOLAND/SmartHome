package smarthome.menu;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import smarthome.database.SystemDAO;
import smarthome.model.user.User;

/**
 * Menu
 * @author Marek Pałdyna
 */
@Component
@Configurable
public class Menu {

    ArrayList<PozycjaMenu> pozycje;
    public Menu(User user, SystemDAO system){
        pozycje = new ArrayList<>();
        if (user!=null && user.getUprawnienia()!=null) {
            if (user.getUprawnienia().isAdmin()) {
                PozycjaMenu tmp = new PozycjaMenu("<i class=\"icon-home\"></i>", "/admin/");
                pozycje.add(tmp);
                
                tmp = new PozycjaMenu("Pokoje", "#", true);
                pozycje.add(tmp);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfRooms"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addRoom"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./removeRoom"));
                
                tmp = new PozycjaMenu("Akcje", "/activities", true);
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addActivity"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./addActivity"));

                tmp = new PozycjaMenu("Urządzenia", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfDevices"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addDevice"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./removeDevice"));
                pozycje.add(tmp);

                tmp = new PozycjaMenu("Sensory", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfSensors"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addSensor"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./removeSensor"));
                pozycje.add(tmp);

                tmp = new PozycjaMenu("Użytkownicy", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfUsers"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addUser"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./removeUser"));
                pozycje.add(tmp);
            }
            else{
                PozycjaMenu tmp = new PozycjaMenu("<i class=\"icon-home\"></i>", "/");
                pozycje.add(tmp);
                tmp = new PozycjaMenu("Akcje", "/activities",true);
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addActivity"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./addActivity"));
                pozycje.add(tmp);
                tmp = new PozycjaMenu("Pokoje", "/rooms");
                pozycje.add(tmp);
                tmp = new PozycjaMenu("Urządzenia", "/devices");
                pozycje.add(tmp);
                tmp = new PozycjaMenu("Sensory", "/sensors");
                pozycje.add(tmp);
                
            }

        }
    }

    public void addPozycja(PozycjaMenu poz) {
        pozycje.add(poz);
    }
    public void delPozycja(PozycjaMenu poz){
        pozycje.remove(poz);
    }
    public PozycjaMenu getPozycja(String name){
        PozycjaMenu tmp = null;
        for (PozycjaMenu pozycjaMenu : pozycje) {
            if(pozycjaMenu.getZawartosc().equals(name))
                tmp = pozycjaMenu;
        }
        return tmp;
    }

    public ArrayList<PozycjaMenu> getPozycje(){
        return pozycje;
    }
}