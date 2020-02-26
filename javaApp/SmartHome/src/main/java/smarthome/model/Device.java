package smarthome.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device
 * 
 * @author Marek Pałdyna
 */
public abstract class Device {
    /** Logger Springa */
    Logger logger;

    /** Id urządzenia w systemie */
    int id; 
    /** ID Pokoju w, którym jest urządzenie */
    int room;
    /** ID urzadzenia na płytce drukowanej */
    int roomID;
    
    public Device() {
        this.id = -1;
        this.room = -1;
        this.roomID = -1;
        logger = LoggerFactory.getLogger(Device.class);
    }
    
    public Device(int id, int room, int roomID){
        this.id = id;
        this.room = room;
        this.roomID = roomID;
        logger = LoggerFactory.getLogger(Device.class);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoom() {
        return this.room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

}