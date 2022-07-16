package smarthome;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import smarthome.automation.ButtonFunction;
import smarthome.database.AutomationDAO;
import smarthome.database.SystemDAO;
import smarthome.model.Room;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonClickType;
import smarthome.model.hardware.Light;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmartHomeAppTest {
	@Autowired 
	AutomationDAO automationDAO;

	@Autowired
	SystemDAO systemDAO;


	@Test
	public void contextLoads() {

		if (!systemDAO.haveAnyRoom()) {
			Room room = new Room(0, "Kitchen");
			systemDAO.addRoom(room);
			Light device = new Light(0, 0, 0, 0);
			room.addDevice(device);
			Button b = new Button(0, 0);
			b.setId(1);
			room.addSensor(b);
			systemDAO.save();
		}
		else {
			Room room = systemDAO.getRoom(0);
			Light device = (Light) room.getDeviceById(0);
			Button b = (Button) room.getSensors().get(0);
			if(automationDAO.getAllFunctions().size()<=0){
				ButtonFunction bf = new ButtonFunction(b, 1, ButtonClickType.CLICKED);
				automationDAO.addFunction(bf);
			}
			else{
				ButtonFunction bf = (ButtonFunction) automationDAO.getAllFunctions().get(0);
				if (b.equals(bf.getButton())) {
					System.out.println( "ButtonFunction is already in the database");
				}
			}
		}

	}

}
