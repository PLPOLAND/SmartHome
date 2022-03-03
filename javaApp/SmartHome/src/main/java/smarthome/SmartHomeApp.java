package smarthome;

import java.util.Scanner;

import com.pi4j.io.i2c.I2CDevice;

import org.apache.catalina.core.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import smarthome.controller.AdminRESTController;
import smarthome.database.SystemDAO;
import smarthome.model.hardware.Termometr;




@SpringBootApplication
@EnableScheduling
public class SmartHomeApp extends SpringBootServletInitializer {
	
	static Scanner scanner = new Scanner(System.in);
	static AdminRESTController adminController;
	static smarthome.system.System system;
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SmartHomeApp.class);
	}

	public static void main(String[] args) throws Exception{
		ConfigurableApplicationContext app = SpringApplication.run(SmartHomeApp.class, args);
		
		while(!app.isRunning())
			;
		adminController = app.getAutowireCapableBeanFactory().getBean(AdminRESTController.class);
		system = app.getAutowireCapableBeanFactory().getBean(smarthome.system.System.class);

		// system.getArduino().atmega.findAll();
		Logger log = LoggerFactory.getLogger("SmartHomeApp");
		
		String in = "";

		while(!in.equals("end") && !in.equals("stop") && ! in.equals("exit")){
			in = scanner.next();
			try{
				if(in.equals("print")){
					log.info(adminController.getSystemData().getObj().toString());
				}
				else if(in.equals("find")){
					log.info(adminController.find().getObj().toString());
				}
				else if(in.equals("addRoom")){
					if(scanner.hasNext()){
						in = scanner.next();
						String nazwaPokoju = in;
						log.info(adminController.dodajPokoj(nazwaPokoju).getObj().toString());
					}
				}
				else if(in.equals("addSwiatlo")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idPlytki= scanner.nextInt();
							if (scanner.hasNext()) {
								int pin= scanner.nextInt();
								
								log.info(adminController.dodajSwiatlo(nazwaPokoju,idPlytki, pin).getObj().toString());
							}
						}
					}
				} 
				else if(in.equals("addRoleta")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int pinUp= scanner.nextInt();
							if (scanner.hasNext()) {
								int pinDown= scanner.nextInt();

								log.info(adminController.dodajRoleta(nazwaPokoju,3, pinUp, pinDown).getObj().toString());
							}
						}
					}
				} else if (in.equals("addTermometr")) {
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int adress = scanner.nextInt();
							
							log.info(adminController.dodajTermometr(nazwaPokoju, adress).getObj().toString());
							
						}
					}
				}
				else if(in.equals("removeDevice")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int id = scanner.nextInt();

							log.info(adminController.removeDevice(nazwaPokoju,id).getObj().toString());
						}
					}
				}
				else if(in.equals("removeRoom")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						log.info(adminController.removeRoom(nazwaPokoju).getObj().toString());
					}
				}
				else if(in.equals("removeTermometr")){

				}
				else if(in.equals("getTemperature")){
					int[] adress = new int[8];
					for (int i = 0; i < adress.length; i++) {
						if(scanner.hasNext()){
							adress[i] = scanner.nextInt();
						}
					}
					log.info("{}",adminController.getTemperatura(adress).getObj());
				}
				else if(in.equals("updateTemperature")){
					for (Termometr termometr : system.getSystemDAO().getAllTermometers()) {
						system.updateTemperature(termometr);
					}
				}
				else if(in.equals("lightON")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idUrzadzenia = scanner.nextInt();
							log.info(adminController.zmienStanSwiatla(nazwaPokoju, idUrzadzenia, true).getObj());

						}
					}
				}
				else if(in.equals("lightOFF")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idUrzadzenia = scanner.nextInt();
							log.info(adminController.zmienStanSwiatla(nazwaPokoju, idUrzadzenia, false).getObj());

						}
					}
				}
				else if(in.equals("blindUP")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idUrzadzenia = scanner.nextInt();
							log.info(adminController.zmienStanRolety(nazwaPokoju, idUrzadzenia, true).getObj());
					
						}
					}
				}
				else if(in.equals("blindDOWN")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idUrzadzenia = scanner.nextInt();
							log.info(adminController.zmienStanRolety(nazwaPokoju, idUrzadzenia, false).getObj());
						}
					}
				}
				else if(in.equals("test")){
					system.getSystemDAO().removeRoom("Marek");
					adminController.dodajPokoj("Marek");

					log.info(adminController.dodajRoleta("Marek", 3, 11,12).getObj());
					log.info("id" + (system.getSystemDAO().getRoom("Marek").getDevices().size()-1));
					log.info(adminController.zmienStanRolety("Marek", system.getSystemDAO().getRoom("Marek").getDevices().get(system.getSystemDAO().getRoom("Marek").getDevices().size()-1).getId(), true).getObj());
					
					
					// log.info(adminController.dodajSwiatlo("Marek", 3, 11));
					// log.info("id" + (system.getSystemDAO().getRoom("Marek").getDevices().size()-1));
					// log.info(adminController.zmienStanSwiatla("Marek", system.getSystemDAO().getRoom("Marek").getDevices().get(system.getSystemDAO().getRoom("Marek").getDevices().size()-1).getId(), true));

					// log.info(adminController.dodajTermometr("Marek", 3).getObj());
					// for (Termometr termometr : system.getSystemDAO().getAllTermometers()) {
					// 	system.updateTemperature(termometr);
					// }
					// int [] tmp = {40,255,30,49,0,22,2,171};
					// log.info(adminController.getTemperatura(tmp).getObj());
				}
				else if (in.equals("reinit")) {
					if (scanner.hasNext()) {
						int idUrzadzenia = scanner.nextInt();
						log.info(adminController.sprawdzZainicjowaniePlytki(idUrzadzenia).getObj());
					}
				}
				else if (in.equals("init")) {
					if (scanner.hasNext()) {
						int idUrzadzenia = scanner.nextInt();
						log.info(adminController.reainicjowaniePlytki(idUrzadzenia).getObj());
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		};
		SpringApplication.exit(app);
	}
}
