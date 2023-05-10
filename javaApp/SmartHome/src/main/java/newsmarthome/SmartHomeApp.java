package newsmarthome;

import java.util.Scanner;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import smarthome.automation.ButtonFunction;
import smarthome.automation.FunctionAction;
import smarthome.controller.AdminRESTController;
import smarthome.exception.HardwareException;
import smarthome.exception.SoftwareException;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonClickType;
import smarthome.model.hardware.ButtonLocalFunction;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceState;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Termometr;




@SpringBootApplication
@EnableScheduling
public class SmartHomeApp extends SpringBootServletInitializer {
	
	static Scanner scanner = new Scanner(System.in);
	static AdminRESTController adminController;
	static smarthome.system.System system;
	private static ConfigurableApplicationContext app;
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SmartHomeApp.class);
	}

	public static ConfigurableApplicationContext getApp(){
		return app;
	}

	public static void main(String[] args) throws Exception{
		app = SpringApplication.run(SmartHomeApp.class, args);
		 
		while(!app.isRunning())
			;
		adminController = app.getAutowireCapableBeanFactory().getBean(AdminRESTController.class);
		system = app.getAutowireCapableBeanFactory().getBean(smarthome.system.System.class);
		// system.reinitAllBoards();
		Logger log = LoggerFactory.getLogger("SmartHomeApp");
		log.info("Started");
		String in = "";

		while(!in.equals("end") && !in.equals("stop") && ! in.equals("exit")){
			in = scanner.next();
			try{
				if(in.equals("print")){
					log.info(adminController.getSystemData().getObj().toString());
					log.info(system.getAutomationDAO().toString());
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
								if (scanner.hasNext()) {
									String name= scanner.next();
									
									log.info(adminController.dodajSwiatlo(nazwaPokoju,name,idPlytki, pin).getObj().toString());
								}
							}
						}
					}
				} 
				else if(in.equals("addRoleta")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idPlytki= scanner.nextInt();
							if (scanner.hasNext()) {
								int pinUp= scanner.nextInt();
								if (scanner.hasNext()) {
									int pinDown= scanner.nextInt();
									if (scanner.hasNext()) {
										String name= scanner.next();
										log.info(adminController.dodajRoleta(nazwaPokoju,name, idPlytki, pinUp, pinDown).getObj().toString());
									}
								}
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
				} else if (in.equals("addPrzycisk")) {
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int idPlytki = scanner.nextInt();
							if (scanner.hasNext()) {
								int pin = scanner.nextInt();

								log.info(adminController.dodajPrzycisk("dodany z terminala",nazwaPokoju, idPlytki, pin).getObj().toString());
							}
						}
					}
				} else if (in.equals("rmPrzyciskClickFunction")) {//TODO sprawdzić
					if (scanner.hasNext()) {
						int idPrzycisku = scanner.nextInt();
						if (scanner.hasNext()) {
							int clicks = scanner.nextInt();
							log.info(adminController.rmButtonClickFunction(idPrzycisku, clicks).getObj());
						}
					}
				} else if (in.equals("addPrzyciskClickFunction")) {//TODO sprawdzić
					if (scanner.hasNext()) {
						int idPrzycisku = scanner.nextInt();
						if (scanner.hasNext()) {
							int clicks = scanner.nextInt();
							if (scanner.hasNext()) {
								int deviceID = scanner.nextInt();
								if (scanner.hasNext()) {
									int tr = scanner.nextInt();
									Device d = system.getDeviceByID(deviceID);
									if (d.getTyp() == DeviceTypes.LIGHT || d.getTyp() == DeviceTypes.GNIAZDKO) {
										log.info(adminController.addButtonClickFunction(idPrzycisku, deviceID, ButtonLocalFunction.State.NONE, clicks).getObj());
									} else {
										log.info(adminController.addButtonClickFunction(idPrzycisku, deviceID, tr == 1 ? ButtonLocalFunction.State.UP : ButtonLocalFunction.State.DOWN, clicks).getObj());
									}
								}
							}
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
					
					
					if (system.isSlaveConnected(15)) {
							
							system.checkInitOfBoard(15);
							system.checkGetAndExecuteCommandsFromSlave(15);
							
					}
					

					// byte[] data = {67, 4, 3, 67, 0, 0, 0, 0};
					// ButtonFunction function = new ButtonFunction();
					// function.fromCommand(15, data);

					// for (ButtonFunction fun : system.getAutomationDAO().getButtonFunctions()) {
					// 	if (fun.compare(function)) {
					// 		log.debug("Znaleziono funkcję: {}", fun);
					// 		fun.run();
					// 		break;
					// 	}

					// }
					// system.getArduino().atmega.writeTo(16, tmp);
					// Thread.sleep(10);
					// log.info("reading from 16: {}",system.getArduino().atmega.readFrom(16, 8));

					// log.info(adminController.addButtonClickFunction(0, 1, ButtonFunction.State.NONE, 1).getObj());
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
				else if (in.equals("update")){
					for (Device device : system.getSystemDAO().getDevices()) {
						try{
							system.updateDeviceState(device);
						}catch(HardwareException e){
							log.error(e.getMessage(), e);
						}
					}		
				}
				else if (in.equals("restart")){
					system.getArduino().atmega.restartSlaves();		
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		};
		SpringApplication.exit(app);
	}
}
