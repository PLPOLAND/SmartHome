package smarthome.menu;

import java.util.ArrayList;

/**
 * Klasa odpowiedzialana za przechowywanie pozycji w menu
 * @author Marek Pałdyna
 */

public class PozycjaMenu {
    String zawartosc;
    String link;
    boolean dropdown;
    ArrayList<PozycjaMenu> dropdownMenu;

    public PozycjaMenu(){
        this.zawartosc ="";
        this.link ="";
        this.dropdown = false;
    }

    public PozycjaMenu(String zawartosc, String adres) {
        this.zawartosc = zawartosc;
        this.link = adres;
        this.dropdown = false;
    }

    public PozycjaMenu(String zawartosc, String adres, boolean dropdown) {
        this.zawartosc = zawartosc;
        this.link = adres;
        this.dropdown = dropdown;
        if (this.dropdown == true) {
            this.dropdownMenu = new ArrayList<>();
        }
    }


    public String getZawartosc() {
        return this.zawartosc;
    }

    public void setZawartosc(String zawartosc) {
        this.zawartosc = zawartosc;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isDropdown() {
        return this.dropdown;
    }

    public boolean getDropdown() {
        return this.dropdown;
    }

    public void setDropdown(boolean dropdown) {
        this.dropdown = dropdown;
    }

    public void addDropDown(PozycjaMenu poz) {
        if(this.dropdownMenu == null)
            dropdownMenu = new ArrayList<>();
        dropdownMenu.add(poz);
    }

    public ArrayList<PozycjaMenu> getDropdownMenu() {
        return this.dropdownMenu;
    }

    public void setDropdownMenu(ArrayList<PozycjaMenu> dropdownMenu) {
        this.dropdownMenu = dropdownMenu;
    }


    @Override
    public String toString() {
        return "{" +
            " zawartosc='" + getZawartosc() + "'" +
            ", link='" + getLink() + "'" +
            ", dropdown='" + isDropdown() + "'" +
            ", dropdownMenu='" + this.dropdownMenu + "'" +
            "}";
    }
    


}
