package smarthome.model;

/**
 * Uprawnienia.
 * Klasa przetrzymująca uprawnienia usera
 * @author Marek Pałdyna
 * @version 1.0
 */
public class Uprawnienia {

    boolean admin;

    public Uprawnienia() {
        admin = false;
    }

    public Uprawnienia(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public boolean getAdmin() {
        return this.admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

}