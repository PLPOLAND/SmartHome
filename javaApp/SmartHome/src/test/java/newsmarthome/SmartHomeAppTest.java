package newsmarthome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import newsmarthome.model.hardware.HardwareFactory;
import newsmarthome.model.hardware.device.Blind;
import newsmarthome.model.hardware.device.DeviceTypes;
import newsmarthome.model.hardware.device.Light;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmartHomeAppTest {
	// @Autowired 
	// AutomationDAO automationDAO;

	// @Autowired
	// SystemDAO systemDAO;

	@Autowired
	HardwareFactory factory;

	@Test
	public void contextLoads() {
		// Light light = (Light) factory.createDevice(DeviceTypes.LIGHT);
		// assert(light.i2c != null);

	}

	@Test
	public void i2cOnDevice(){
		assertNotNull(factory.createLight().slaveSender );
		// assertEquals(1,light.getPin());
		assertNotNull(factory.createFan());
		assertNotNull(factory.createOutlet());
		assertNotNull(factory.createBlind());
	}
	@Test
	public void hardwareFactoryTest(){
		assertEquals(1, factory.createLight(1).getPin());
		assertEquals(1, factory.createFan(1).getPin());
		assertEquals(1, factory.createOutlet(1).getPin());
		Blind blind = factory.createBlind(1,2);
		assertEquals(1, blind.getPinUp());
		assertEquals(2, blind.getPinDown());
	}

}
