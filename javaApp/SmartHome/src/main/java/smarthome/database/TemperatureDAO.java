package smarthome.database;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import smarthome.model.Termometr;

/**
 * TemperatureDAO
 */
@Repository
public class TemperatureDAO {

    List<Termometr> temps;

    public TemperatureDAO(){
        temps = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger(UsersDAO.class);
        logger.info("Init");
        temps.add(new Termometr());
        // temps.get(0).setTemperatura(0.0);
    }
    public void addTemp(Termometr temp){
        temps.add(temp);
    }
    
    public Termometr getTemp(int i){
        return temps.get(i);
    }
    
    public boolean setTemp(int i, Double temp){
        temps.get(i).setTemperatura(temp);
        return true;
    }
    public boolean setTemp(int i, Termometr temp){
        temps.set(i,temp);
        return true;
    }

}