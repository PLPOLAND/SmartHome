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
import smarthome.model.Response;
import smarthome.model.Room;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Switch;
import smarthome.model.hardware.Termometr;
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
        byte[] tmp = {'x'};
        byte[] msg = {'A','P',11};
        try {
            converter.atmega.writeTo(3, msg, 3);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            tmp = converter.getAnything(3);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Response response = new Response<>(tmp.toString(), "errortmp");
        // // converter.changeSwitchState(new Przekaznik(1,1,1,4), true);
        return response;
    }
    @RequestMapping("/find")
    public Response find() {
        new smarthome.i2c.I2C();
        Response response = new Response<>("", "errortmp");

        // // converter.changeSwitchState(new Przekaznik(1,1,1,4), true);
        return response;
    }

    @RequestMapping("/sentAny")
    public Response<String> sentAny(@RequestParam("msg") String msg, @RequestParam("adres") int adres) {
        Response r = new Response<String>(msg+" " + adres);
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


    @RequestMapping("/getSystemData")
    public Response<SystemDAO> getSystemData() {
        return new Response<SystemDAO>(system);
    }

    @GetMapping("/addRoom")
    public Response<String> dodajPokoj(@RequestParam("name") String name){
        Room r = new Room(roomsID++, name);
        system.addRoom(r);

        return new Response<String>("Pokój: '" + name +"' dodany");
    }

    // public Response<String> setIdPlytkiRoom(@RequestParam("name") String name, @RequestParam("id") int id) {
    //     system.getRoom(name).s

    // }

    // @GetMapping("/addGniazdko")
    // public Response<String> dodajGniazdko(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin){
    //     Switch g;
    //     try {
    //         g = new Switch(false, pin);
    //         system.addDeviceToRoom(nazwaPokoju, g);
    //     } catch (Exception e) {
    //         return new Response<>("",e.getMessage());
    //     }
    //     return new Response<String>("Gniazdko: '" + g.toString() + "' dodane prawidłowo");

    // }
    @GetMapping("/addSwiatlo")
    public Response<String> dodajSwiatlo(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin){
        Light l;
        try {
            l = new Light(pin);
            system.addDeviceToRoom(nazwaPokoju, l);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("",e.getMessage());
        }
        return new Response<String>("Swiatlo: '" + l.toString() + "' dodane prawidłowo");

    }
    @GetMapping("/addTermometr")
    public Response<String> dodajTermometr(@RequestParam("name") String nazwaPokoju,@RequestParam("pin") int pin, @RequestParam("idPlytki") int idPlytki){
        Termometr t;
        try {
            t = new Termometr(pin);
            t.setIDPlytki(idPlytki);
            // converter.addTermometr(t);//TODO  
            system.addSensorToRoom(nazwaPokoju, t);
        } catch (Exception e) {
            return new Response<>("",e.getMessage());
        }
        return new Response<String>("Termometr: '" + t.toString() + "' dodany prawidłowo");

    }



}
