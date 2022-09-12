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

		

	}

}
