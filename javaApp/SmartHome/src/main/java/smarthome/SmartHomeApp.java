package smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

@SpringBootApplication
@EnableScheduling
public class SmartHomeApp extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SmartHomeApp.class);
	}

	public static void main(String[] args) throws Exception{
		// SpringApplication.run(SmartHomeApp.class, args);
		List<Integer> validAddresses = new ArrayList<Integer>();
		final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		for (int i = 1; i < 128; i++) {
			try {
				I2CDevice device = bus.getDevice(i);
				device.write((byte) 0);
				byte[] buffer = new byte[8];
				device.read(buffer, 0, 8);
				for (byte b : buffer) {
					System.out.print((char)b);
				}
				validAddresses.add(i);
			} catch (Exception ignore) {
			}
		}

		System.out.println("Found: ---");
		for (int a : validAddresses) {
			System.out.println("Address: " + Integer.toHexString(a));
		}
		System.out.println("----------");
	}
}
