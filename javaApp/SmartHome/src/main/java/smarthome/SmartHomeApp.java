package smarthome;

import java.util.Scanner;

import org.apache.catalina.core.ApplicationContext;
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
		String in = "";

		while(!in.equals("end")){
			in = scanner.next();
			try{
				if(in.equals("print")){
					System.out.println(adminController.getSystemData().getObj().toString());
				}
				else if(in.equals("find")){
					System.out.println(adminController.find().getObj().toString());
				}
				else if(in.equals("addRoom")){
					if(scanner.hasNext()){
						in = scanner.next();
						String nazwaPokoju = in;
						System.out.println(adminController.dodajPokoj(nazwaPokoju).getObj().toString());
					}
				}
				else if(in.equals("addSwiatlo")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int pin= scanner.nextInt();

							System.out.println(adminController.dodajSwiatlo(nazwaPokoju,3, pin).getObj().toString());
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

								System.out.println(adminController.dodajRoleta(nazwaPokoju,3, pinUp, pinDown).getObj().toString());
							}
						}
					}
				} else if (in.equals("addTermometr")) {
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						
						System.out.println(adminController.dodajTermometr(nazwaPokoju, 3).getObj().toString());
					}
				}
				else if(in.equals("removeDevice")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						if (scanner.hasNext()) {
							int id = scanner.nextInt();

							System.out.println(adminController.removeDevice(nazwaPokoju,id).getObj().toString());
						}
					}
				}
				else if(in.equals("removeRoom")){
					if (scanner.hasNext()) {
						String nazwaPokoju = scanner.next();
						System.out.println(adminController.removeRoom(nazwaPokoju).getObj().toString());
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
