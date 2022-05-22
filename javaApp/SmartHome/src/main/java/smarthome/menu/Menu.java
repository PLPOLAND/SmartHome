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

    ArrayList<PozycjaMenu> menuGlowne;
    ArrayList<PozycjaMenu> menuUsera;

    public Menu(User user, SystemDAO system){
        menuGlowne = new ArrayList<>();
        menuUsera = new ArrayList<>();
        if (user!=null && user.getUprawnienia()!=null) {
            if (user.getUprawnienia().isAdmin()) {
                PozycjaMenu tmp = new PozycjaMenu("<i class=\"icon-home\"></i>", "/admin/");
                menuGlowne.add(tmp);
                
                tmp = new PozycjaMenu("Pokoje", "#", true);
                menuGlowne.add(tmp);
                tmp.addDropDown(new PozycjaMenu("Lista", "./roomsList"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addRoom"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./removeRoom"));
                
                tmp = new PozycjaMenu("Akcje", "/activities", true);
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addActivity"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./addActivity"));

                tmp = new PozycjaMenu("Urządzenia", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfDevices"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addDevice"));
                // tmp.addDropDown(new PozycjaMenu("Usuń", "./removeDevice"));
                menuGlowne.add(tmp);

                tmp = new PozycjaMenu("Sensory", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfSensors"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addSensor"));
                // tmp.addDropDown(new PozycjaMenu("Usuń", "./removeSensor"));
                menuGlowne.add(tmp);

                tmp = new PozycjaMenu("Użytkownicy", "#", true);
                tmp.addDropDown(new PozycjaMenu("Lista", "./listOfUsers"));
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addUser"));
                // tmp.addDropDown(new PozycjaMenu("Usuń", "./removeUser"));
                menuGlowne.add(tmp);

                //MENU USERA
                tmp = new PozycjaMenu("Wyloguj <i class=\"icon-logout\"></i>", "./logout");
                menuUsera.add(tmp);
                tmp = new PozycjaMenu("Ustawienia <i class=\"icon-sliders\"></i>", "./userSetings");
                menuUsera.add(tmp);
                tmp = new PozycjaMenu("Wyłącz System <i class=\"icon-off\"></i>", "./shutdown");
                menuUsera.add(tmp);
                


            }
            else{
                PozycjaMenu tmp = new PozycjaMenu("<i class=\"icon-home\"></i>", "/");
                menuGlowne.add(tmp);
                tmp = new PozycjaMenu("Akcje", "/activities",true);
                tmp.addDropDown(new PozycjaMenu("Dodaj", "./addActivity"));
                tmp.addDropDown(new PozycjaMenu("Usuń", "./addActivity"));
                menuGlowne.add(tmp);
                tmp = new PozycjaMenu("Pokoje", "/rooms");
                menuGlowne.add(tmp);
                tmp = new PozycjaMenu("Urządzenia", "/devices");
                menuGlowne.add(tmp);
                tmp = new PozycjaMenu("Sensory", "/sensors");
                menuGlowne.add(tmp);
                

                // MENU USERA
                tmp = new PozycjaMenu("Wyloguj <i class=\"icon-logout\"></i>", "./logout");
                menuUsera.add(tmp);
                tmp = new PozycjaMenu("Ustawienia <i class=\"icon-sliders\"></i>", "./userSetings");
                menuUsera.add(tmp);
            }

        }
    }

    public void addPozycjaMenuGlowne(PozycjaMenu poz) {
        menuGlowne.add(poz);
    }
    public void delPozycjaMenuGlowne(PozycjaMenu poz){
        menuGlowne.remove(poz);
    }
    public PozycjaMenu getPozycjaMenuGlowne(String name){
        PozycjaMenu tmp = null;
        for (PozycjaMenu pozycjaMenu : menuGlowne) {
            if(pozycjaMenu.getZawartosc().equals(name))
                tmp = pozycjaMenu;
        }
        return tmp;
    }

    public void addPozycjaMenuUsera(PozycjaMenu poz) {
        menuUsera.add(poz);
    }
    public void delPozycjaMenuUsera(PozycjaMenu poz){
        menuUsera.remove(poz);
    }
    public PozycjaMenu getPozycjaMenuUsera(String name){
        PozycjaMenu tmp = null;
        for (PozycjaMenu pozycjaMenu : menuUsera) {
            if(pozycjaMenu.getZawartosc().equals(name))
                tmp = pozycjaMenu;
        }
        return tmp;
    }

    public ArrayList<PozycjaMenu> getMenuGlowne(){
        return menuGlowne;
    }
    public ArrayList<PozycjaMenu> getMenuUsera(){
        return menuUsera;
    }
}