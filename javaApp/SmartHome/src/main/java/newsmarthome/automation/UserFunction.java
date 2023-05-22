package newsmarthome.automation;

import newsmarthome.exception.HardwareException;
import newsmarthome.model.Room;
import newsmarthome.model.user.User;

/**
 * Funkcja wywoływana przez Usera
 */
public class UserFunction extends Function {

    private User user;
    /** Czy tą funkcję pokazywać tylko urzytkownikowi, który ją stworzył */
    boolean privateFunction;
    Room room;//pokój do którego jest przypisana funkcja


    public UserFunction() {
        super( FunctionType.USER );
        user = null;
        privateFunction = false;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPrivateFunction() {
        return privateFunction;
    }
    
    public void setPrivateFunction(boolean privateFunction) {
        this.privateFunction = privateFunction;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void run() throws HardwareException {
        if (this.isReversState()) {
            for (FunctionAction action : this.actions) {
                action.run();
            }
        }
        else{
            if (this.isActive()) {
                this.deactivate();
            }
            else{
                this.activate();
            }           
        }
        
    }

    @Override
    public void activate() throws HardwareException {
        for (FunctionAction action : this.actions) {
            action.activate();
        }
        
    }

    @Override
    public void deactivate() throws HardwareException {
        for (FunctionAction action : this.actions) {
            action.deactivate();
        }
        
    }
    
}
