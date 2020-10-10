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
import smarthome.model.Gniazdko;
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

    int roomsID = 0;// id nowego pokoju.

    @RequestMapping("/test")
    public Response test() {
        Response response = new Response<>(system.getRoomsArrayList(),"errortmp");
        return response;
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
        Gniazdko g;
        try {
            g = new Gniazdko(false, pin);
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
