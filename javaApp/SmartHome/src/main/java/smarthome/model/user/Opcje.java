package smarthome.model.user;

import org.springframework.stereotype.Component;


@Component
/**
 * @author Marek Pałdyna
 */
public class Opcje {
    
    //#region Avatar
    enum AvatarType{
        NONE,
        GRAVATAR,
        LOCAL
    }
    
    public Opcje(){
        typAvatara = AvatarType.GRAVATAR;
        lokalnaSciezka = "";
    }

    public Opcje(String path){
        lokalnaSciezka = path;
    }

    AvatarType typAvatara; // typ lokalizacji avatara
    String lokalnaSciezka;
    
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


    @Override
    public String toString() {
        return "{" +
            " typAvatara='" + getTypAvatara() + "'" +
            ", lokalnaSciezka='" + getLokalnaSciezka() + "'" +
            "}";
    }


}
