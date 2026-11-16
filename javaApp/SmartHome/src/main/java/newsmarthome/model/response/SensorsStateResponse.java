package newsmarthome.model.response;

import java.util.ArrayList;
import java.util.List;


public class SensorsStateResponse<T> {
    private int id;
    private ArrayList<T> state;

    public SensorsStateResponse(int id, ArrayList<T> states) {
        this.id = id;
        this.state = states;
    }

    public SensorsStateResponse(){
        this.id = -1;
        this.state = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public List<T> getState() {
        return this.state;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setState(ArrayList<T> state) {
        this.state = state;
    }

    public void addState(T state) {
        this.state.add(state);
    }




}
