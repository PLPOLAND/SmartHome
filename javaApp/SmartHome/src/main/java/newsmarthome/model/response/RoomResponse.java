package newsmarthome.model.response;

import newsmarthome.model.Room;

public class RoomResponse {
    int id;
    String name;

    public RoomResponse(int id, String name){
        this.id = id;
        this.name = name;
    }

    public RoomResponse(Room room){
        this.id = room.getID();
        this.name = room.getName();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

}
