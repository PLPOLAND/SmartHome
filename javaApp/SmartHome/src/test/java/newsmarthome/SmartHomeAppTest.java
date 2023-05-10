package newsmarthome;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import newsmarthome.model.hardware.HardwareFactory;
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
		Light light = (Light) factory.createDevice(DeviceTypes.LIGHT);
		assert(light.i2c != null);

	}

}
