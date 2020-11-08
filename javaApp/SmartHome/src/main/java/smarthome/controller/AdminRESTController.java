package smarthome.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smarthome.database.SystemDAO;
import smarthome.database.UsersDAO;
import smarthome.i2c.JtAConverter;
import smarthome.model.Przekaznik;
import smarthome.model.Light;
import smarthome.model.Response;
import smarthome.model.Room;
import smarthome.model.Termometr;
import smarthome.security.Security;

@RestController
@RequestMapping("/admin/api")
public class AdminRESTController {
    @Autowired
    SystemDAO system;
    @Autowired
    UsersDAO users;

    @Autowired
    JtAConverter converter;

    int roomsID = 0;// id nowego pokoju.

    @RequestMapping("/test")
    public Response test() {
        Response response = new Response<>(system.getRoomsArrayList(), "errortmp");

        converter.checkTemperature(new Termometr(1, 1, 8, 10, 0.0f, 0.0f, 0.0f));
        // converter.changeSwitchState(new Przekaznik(1,1,1,4), true);
        return response;
    }

    @RequestMapping("/sentAny")
    public Response<String> sentAny(@RequestParam("msg") String msg, @RequestParam("adres") int adres) {
        Response r = new Response<String>(msg);
        converter.sentAnything(msg, adres);
        return r;
    }

    @RequestMapping("/ReadAny")
    public Response<String> readAny(@RequestParam("adres") int adres) {
        Response r;
        try {
            r = new Response<String>(new String(converter.getAnything(adres)));
        } catch (Exception e) {
            r = new Response<String>(e.getMessage());
        }
        
        return r;
    }

    @RequestMapping("/")
    public Date main(){
        return new Date(System.currentTimeMillis());
    }


    @RequestMapping("/login")
    Response<String> login(HttpServletRequest request) {
        Security s = new Security(request, users);
        if (s.login())
            return new Response<>("/admin/");
        else
            return new Response<String>("","Nie znaleziono dopasowania w bazie danych");
    }





    @GetMapping("/addRoom")
    public Response<String> dodajPokoj(@RequestParam("name") String name){
        Room r = new Room(roomsID++, name);
        system.addRoom(r);

        return new Response<String>("Pokój: '" + name +"' dodany");
    }
    @GetMapping("/addGniazdko")
    public Response<String> dodajGniazdko(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin){
        Przekaznik g;
        try {
            g = new Przekaznik(false, pin);
            system.addDeviceToRoom(nazwaPokoju, g);
        } catch (Exception e) {
            return new Response<>("",e.getMessage());
        }
        return new Response<String>("Gniazdko: '" + g.toString() + "' dodane prawidłowo");

    }
    @GetMapping("/addSwiatlo")
    public Response<String> dodajSwiatlo(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin){
        Light l;
        try {
            l = new Light(pin);
            system.addDeviceToRoom(nazwaPokoju, l);
        } catch (Exception e) {
            return new Response<>("",e.getMessage());
        }
        return new Response<String>("Swiatlo: '" + l.toString() + "' dodane prawidłowo");

    }
    @GetMapping("/addTermometr")
    public Response<String> dodajTermometr(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin){
        Termometr t;
        try {
            t = new Termometr(pin);
            system.addDeviceToRoom(nazwaPokoju, t);
        } catch (Exception e) {
            return new Response<>("",e.getMessage());
        }
        return new Response<String>("Termometr: '" + t.toString() + "' dodany prawidłowo");

    }



}
