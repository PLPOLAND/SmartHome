package newsmarthome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import newsmarthome.database.SystemDAO;
import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.Device;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;
import newsmarthome.model.hardware.sensor.Button;
import newsmarthome.model.hardware.sensor.Sensor;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmartHomeAppTest {
	// @Autowired 
	// AutomationDAO automationDAO;

	@Autowired
	SystemDAO systemDAO;

	@Autowired
	HardwareFactory factory;

	@Test
	public void contextLoads() {

	}

	@Test
	public void i2cOnDevice(){
		assertNotNull(factory.createLight().slaveSender );
		// assertEquals(1,light.getPin());
		assertNotNull(factory.createFan().slaveSender);
		assertNotNull(factory.createOutlet().slaveSender);
		assertNotNull(factory.createBlind().slaveSender);
		assertNotNull(factory.createTermometr().getSlaveSender());
		assertNotNull(factory.createButton().getSlaveSender());
	}
	@Test
	public void hardwareFactoryTest(){
		assertEquals(1, factory.createLight(1).getPin());
		assertEquals(1, factory.createFan(1).getPin());
		assertEquals(1, factory.createOutlet(1).getPin());
		Blind blind = factory.createBlind(1,2);
		assertEquals(1, blind.getPinUp());
		assertEquals(2, blind.getPinDown());

		assertEquals(1, factory.createButton(1).getSlaveAdress());
		Button button = factory.createButton(1,2);
		assertEquals(2, button.getSlaveAdress());
		assertEquals(1, button.getPin());
		

	}
	@Test
	public void fromFileLoad(){
		//TODO testy wczytywania z pliku
		for (Device blind : systemDAO.getDevices()) {
			assertNotNull(blind);
			if (blind instanceof Blind) {
				// System.out.println(((Blind) blind).getSlaveID());
				assertNotEquals(-1, ((Blind) blind).getSlaveID());
			}
		}
		for (Sensor sensor: systemDAO.getAllButtons()){
			assertNotNull(sensor);
			if (sensor instanceof Button) {
				// System.out.println(((Button) sensor).getSlaveAdress());
				assertNotEquals(-1, ((Button) sensor).getSlaveAdress());
			}
		}
	}

}
