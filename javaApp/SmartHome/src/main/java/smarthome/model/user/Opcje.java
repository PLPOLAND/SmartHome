package smarthome.model.user;

import org.springframework.stereotype.Component;


@Component
/**
 * @author Marek Pałdyna
 */
public class Opcje {
    
    private static final String CSS_THEMES_LOCATION = "../css/themes/";


    //#region Avatar
    enum AvatarType{
        NONE,
        GRAVATAR,
        LOCAL
    }
    
    AvatarType typAvatara; // typ lokalizacji avatara
    String lokalnaSciezka;
    
    String themeSciezka;//ścieżka do kolorystyki strony

    public Opcje(){
        typAvatara = AvatarType.LOCAL;
        lokalnaSciezka = "";
        themeSciezka = "";
    }

    public Opcje(String path){
        lokalnaSciezka = path;
    }

    
    //#endregion
    public AvatarType getTypAvatara() {
        return this.typAvatara;
    }

    public void setTypAvatara(AvatarType typAvatara) {
        this.typAvatara = typAvatara;
    }

    public String getLokalnaSciezka() {
        return this.lokalnaSciezka;
    }

    public void setLokalnaSciezka(String lokalnaSciezka) {
        this.lokalnaSciezka = lokalnaSciezka;
    }

    public Opcje typAvatara(AvatarType typAvatara) {
        setTypAvatara(typAvatara);
        return this;
    }

    public Opcje lokalnaSciezka(String lokalnaSciezka) {
        setLokalnaSciezka(lokalnaSciezka);
        return this;
    }


    public String getThemeSciezka() {
        return this.themeSciezka;
    }

    public void setThemeSciezka(String themeSciezka) {
        this.themeSciezka = CSS_THEMES_LOCATION + themeSciezka;
    }


    @Override
    public String toString() {
        return "{" +
            " typAvatara='" + getTypAvatara() + "'" +
            ", lokalnaSciezka='" + getLokalnaSciezka() + "'" +
            ", themeSciezka='" + getThemeSciezka() + "'" +
            "}";
    }

}
