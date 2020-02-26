package smarthome.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.database.TemperatureDAO;
import smarthome.database.UsersDAO;
import smarthome.model.Temperature;
import smarthome.model.Termometr;
import smarthome.security.Security;

/**
 * RestController
 */
@RestController
@RequestMapping("/api")
public class RESTController {
    @Autowired
    UsersDAO users;
    @Autowired
    TemperatureDAO temp;

    @RequestMapping("/login")
    String login(HttpServletRequest request) {
        Security s = new Security(request, users);

        if (s.login())
            return "/";
        else
            return null;
    }

    @GetMapping("/setRGB")
    void setRGB(@RequestParam("rgb") String rgb) {
        String uri = "http://192.168.1.3/setRGB?" + rgb;

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

    }

    @GetMapping("/temp")
    String temp(@RequestParam("t") Double temperatura) {
        temp.setTemp(0, temperatura);
        String wiadomosc = "Poszło ok " + temp.getTemp(0);
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info(temperatura.doubleValue() + "");
        return wiadomosc;
    }

    @GetMapping("/gettemp")
    Termometr gettemp() {
        return temp.getTemp(0);
    }

     @Scheduled(fixedRate = 1000)
    void test() {
        Logger logger = LoggerFactory.getLogger(this.getClass());

        final String uri = "http://192.168.1.3/get";

        ObjectMapper obj = new ObjectMapper();

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        try {
            Temperature termometr2 = obj.readValue(result, Temperature.class);
            logger.info("Temperatura 0: " + termometr2.getTemp());
            temp.setTemp(0, termometr2.getTemp());
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}