package smarthome.database;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import smarthome.model.Temperature;

/**
 * TemperatureDAO
 */
@Repository
public class TemperatureDAO {

    List<Temperature> temps;

    public TemperatureDAO(){
        temps = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger(UsersDAO.class);
        logger.info("Init");
        temps.add(new Temperature());
        temps.get(0).setTemp(0);
    }
    public void addTemp(Temperature temp){
        temps.add(temp);
    }
    
    public Temperature getTemp(int i){
        return temps.get(i);
    }
    
    public boolean setTemp(int i, Double temp){
        temps.get(i).setTemp(temp);
        return true;
    }
    public boolean setTemp(int i, Temperature temp){
        temps.set(i,temp);
        return true;
    }

}