package smarthome.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smarthome.database.SystemDAO;
import smarthome.database.TemperatureDAO;
import smarthome.database.UsersDAO;
import smarthome.model.Response;
import smarthome.model.hardware.Device;
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
    @Autowired
    SystemDAO systemDAO;

    @RequestMapping("/login")
    Response<String> login(HttpServletRequest request) {
        Security s = new Security(request, users);

        if (s.login())
            return new Response<String>("/");
        else
            return new Response<String>("Logowanie nie powiodło się!", "Bledny login lub haslo");
    }
    
    @RequestMapping("/themePath")
    Response<String> getThemePasth(HttpServletRequest request){

        Security s = new Security(request, users);

        if (s.isLoged()){
            return new Response<String>(s.getUserThemePath());
        }
        else{
            return new Response<String>(null, "Nie zalogowano");
        }       
    }

    @RequestMapping("/getSystemData")
    public Response<SystemDAO> getSystemData() {
        return new Response<>(systemDAO);
    }

    @RequestMapping("/getDevices")
    public Response<List<Device>> getDevices(){
        return new Response<>(systemDAO.getDevices());
    }
    @RequestMapping("/getTermometers")
    public Response<List<Termometr>> getTermometers(){
        return new Response<>(systemDAO.getAllTermometers());
    }
}