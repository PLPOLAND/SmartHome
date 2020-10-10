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
    
    AvatarType typAvatara; // typ lokalizacji avatara
    String lokalnaSciezka;
    
    //#endregion

}
