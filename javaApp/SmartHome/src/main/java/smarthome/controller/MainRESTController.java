package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.database.TemperatureDAO;
import smarthome.database.UsersDAO;
import smarthome.model.Response;
import smarthome.model.hardware.Termometr;
import smarthome.security.Security;

/**
 * RestController
 */
@RestController
@RequestMapping("/api")
public class MainRESTController {
    @Autowired
    UsersDAO users;

    @RequestMapping("/login")
    Response login(HttpServletRequest request) {
        Security s = new Security(request, users);

        if (s.login())
            return new Response<String>("/");
        else
            return new Response<String>("Logowanie nie powiodło się!", "Bledny login lub haslo");
    }
    
    

    //  @Scheduled(fixedRate = 1000)
    // void test() {
    //     Logger logger = LoggerFactory.getLogger(this.getClass());

    //     final String uri = "http://192.168.1.3/get";

    //     ObjectMapper obj = new ObjectMapper();

    //     RestTemplate restTemplate = new RestTemplate();
    //     String result = restTemplate.getForObject(uri, String.class);

    //     try {
    //         Temperature termometr2 = obj.readValue(result, Temperature.class);
    //         logger.info("Temperatura 0: " + termometr2.getTemp());
    //         temp.setTemp(0, termometr2.getTemp());
    //     } catch (JsonParseException e) {
    //         e.printStackTrace();
    //     } catch (JsonMappingException e) {
    //         e.printStackTrace();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }

    // }
}