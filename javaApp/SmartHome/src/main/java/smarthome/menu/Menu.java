package smarthome.menu;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
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
        // system = ApplicationContextHolder.getContext().getBean(SystemDAO.class);
        pozycje = new ArrayList<>();
        if (user!=null && user.getUprawnienia()!=null) {
            if (user.getUprawnienia().isAdmin()) {
                pozycje.add(new PozycjaMenu("Dodawanie", "#", true));
                if (this.getPozycja("Dodawanie")!=null) {
                    PozycjaMenu t = this.getPozycja("Dodawanie");
                    t.addDropDown(new PozycjaMenu("Dodaj pokoje", "./addRoom"));
                    if (system.haveRoom()) {
                        t.addDropDown(new PozycjaMenu("Dodaj żarówki", "./addLight"));
                        t.addDropDown(new PozycjaMenu("Dodaj przekaznik", "./addSwitch"));
                        t.addDropDown(new PozycjaMenu("Dodaj termometr", "./addTermometr"));
                    }
                }
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