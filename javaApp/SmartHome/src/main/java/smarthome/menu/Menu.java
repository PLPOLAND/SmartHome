package smarthome.menu;

import java.util.ArrayList;

import smarthome.model.Response;
import smarthome.model.User;

/**
 * Menu
 * @author Marek Pałdyna
 */
public class Menu {

    ArrayList<PozycjaMenu> pozycje;
    public Menu(User user){
        pozycje = new ArrayList<>();
        if (user!=null) {
            if (user.getUprawnienia().isAdmin()) {
                pozycje.add(new PozycjaMenu("Dodaj pokoje", "./addRoom"));
                pozycje.add(new PozycjaMenu("Dodaj żarówki", "./addRoom"));
            }
        }
        


    }

    public void addPozycja(PozycjaMenu poz) {
        pozycje.add(poz);
    }
    public void delPozycja(PozycjaMenu poz){
        pozycje.remove(poz);
    }

    public ArrayList<PozycjaMenu> getPozycje(){
        return pozycje;
    }
}