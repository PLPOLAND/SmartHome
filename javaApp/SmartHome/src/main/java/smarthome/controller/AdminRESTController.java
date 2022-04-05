package smarthome.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smarthome.database.SystemDAO;
import smarthome.database.UsersDAO;
import smarthome.exception.HardwareException;
import smarthome.i2c.JtAConverter;
import smarthome.model.Response;
import smarthome.model.Room;
import smarthome.model.Uprawnienia;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.Termometr;
import smarthome.model.hardware.Blind.RoletaStan;
import smarthome.model.user.Opcje;
import smarthome.model.user.User;
import smarthome.security.Security;

@RestController
@RequestMapping("/admin/api")
public class AdminRESTController {
    /** Logger Springa */
    Logger logger;

    @Autowired
    SystemDAO systemDAO;

    @Autowired
    smarthome.system.System system;

    @Autowired
    UsersDAO users;

    @Autowired
    JtAConverter converter;

    int roomsID = 0;// id nowego pokoju.

    AdminRESTController(){
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @RequestMapping("/")
    public Date main() {
        return new Date(System.currentTimeMillis());
    }

    @RequestMapping("/login")
    Response<String> login(HttpServletRequest request) {
        Security s = new Security(request, users);
        if (s.login())
            return new Response<>("/admin/");
        else
            return new Response<String>("", "Nie znaleziono dopasowania w bazie danych");
    }

    @RequestMapping("/test")
    public Response test() {
        users.createUser( new User(0l, "Marek", "Paldyna", "PLPOLAND", "marekpaldyna@wp.pl", "Mareczek", "xxx", new Uprawnienia(true), new Opcje("../img/users/12322601_643168719175369_7590023013141216084_o.jpg") ));
        return new Response<String>("test");
    }
    @RequestMapping("/find")
    public Response<ArrayList<I2CDevice>> find() {
        try {
            converter.atmega.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(null, e.getMessage());
        }
        return new Response<>((ArrayList<I2CDevice>)converter.atmega.getDevices());
    }

    @RequestMapping("/sendAny")
    public Response<String> sentAny(@RequestParam("msg") String msg, @RequestParam("adres") int adres) {
        Response<String> r = new Response<>(msg+" " + adres);
        converter.sendAnything(msg, adres);
        return r;
    }

    @RequestMapping("/readAny")
    public Response<String> readAny(@RequestParam("adres") int adres) {
        Response<String> r;
        try {
            r = new Response<>(new String(converter.getAnything(adres)));
        } catch (Exception e) {
            r = new Response<>(null, e.getMessage());
        }
        
        return r;
    }

    


    @RequestMapping("/getSystemData")
    public Response<SystemDAO> getSystemData() {
        return new Response<>(systemDAO);
    }

    @GetMapping("/addRoom")
    public Response<String> dodajPokoj(@RequestParam("name") String name){
        Room r = new Room(roomsID++, name);
        systemDAO.addRoom(r);

        return new Response<>("Pokój: '" + name +"' dodany");
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
    public Response<String> dodajSwiatlo(@RequestParam("name") String nazwaPokoju,@RequestParam("boardID") int boardID, @RequestParam("pin") int pin){
        
        try {
            Light l = (Light) system.addLight(nazwaPokoju, boardID, pin);
            if (l != null)
                return new Response<String>("Żarówka: '" + l.toString() + "' dodana prawidłowo");
            else
                return new Response<String>("","Nie udało dodać się Żarówki. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania światła",e);
            return new Response<String>(null, e.getMessage());
        }
        

    }
    @GetMapping("/addRoleta")
    public Response<String> dodajRoleta(@RequestParam("name") String nazwaPokoju,@RequestParam("boardID") int boardID, @RequestParam("pinUp") int pinUp, @RequestParam("pinDown") int pinDown){
        
        
        try {
            Blind l = (Blind) system.addRoleta(nazwaPokoju, boardID, pinUp, pinDown);
            if(l != null)
                return new Response<String>("Roleta: '" + l.toString() + "' dodana prawidłowo");
            else
                return new Response<String>("", "Nie udało dodać się Rolety. Sprawdź konsolę programu w poszukiwaniu szczegółów");
                
            } catch (HardwareException e) {
                return new Response<String>(null,e.getMessage());
            }

    }

    @GetMapping("/removeDevice")

    public Response<String> removeDevice(@RequestParam("name") String nazwaPokoju,@RequestParam("id") int id){
        
        Device dev = null;
        try {
            if((dev = systemDAO.getRoom(nazwaPokoju).getDeviceById(id)) == null)
                throw new Exception("Brak urzadzenia o id: "+id+" w pokoju: "+nazwaPokoju);
            systemDAO.getRoom(nazwaPokoju).delDevice(dev);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
        return new Response<String>("Urzadzenie: '" + dev.toString() + "' usnięte prawidłowo z pokoju: " + nazwaPokoju);
    }

    @GetMapping("/removeRoom")
    public Response<String> removeRoom(@RequestParam("name") String nazwaPokoju) {
        Room room = null;
        try {
            if ((room = systemDAO.getRoom(nazwaPokoju)) == null)
                throw new Exception("Brak pokoju o nazwie: "+ nazwaPokoju);
            systemDAO.removeRoom(room);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
        return new Response<String>("Urzadzenie: '" + room.toString() + "' usnięte prawidłowo z pokoju: " + nazwaPokoju);
    }

    @GetMapping("/addTermometr")
    public Response<String> dodajTermometr(@RequestParam("name") String nazwaPokoju, @RequestParam("idPlytki") int idPlytki){
        Termometr t = system.addTermometr(nazwaPokoju, idPlytki);
        return new Response<>("Termometr: '" + t.toString() + "' dodany prawidłowo");

    }

    @GetMapping("/getTemperatura")
    public Response<Float> getTemperatura(@RequestParam("adres") int[] adress){
        

        return new Response<>(system.getTemperature(adress));
    }

    @GetMapping("/changeLightState")
    public Response<String> zmienStanSwiatla(@RequestParam("name") String nazwaPokoju, @RequestParam("idUrzadzenia") int idUrzadzenia, @RequestParam("stan") boolean stan) {
        
        try {
            Device d =system.changeLightState( nazwaPokoju, idUrzadzenia, stan);
            return new Response<>("Zmieniono stan Swiatla :" +((Light)d).toString()+" na stan: " + (stan == true ? "ON" : "OFF"));
        } catch (Exception e) {
            logger.error("Błąd podczas zmieniania stanu światła! ",e);
            return new Response<>("", e.getMessage());
        }



    }
    @PostMapping("/changeLightStateByRoomID")
    public Response<String> zmienStanSwiatlaByRoomID(@RequestParam("roomID") int roomID, @RequestParam("idUrzadzenia") int idUrzadzenia, @RequestParam("stan") boolean stan) {
        
        try {
            Device d =system.changeLightState( roomID, idUrzadzenia, stan);
            return new Response<>("Zmieniono stan Swiatla :" +((Light)d).toString()+" na stan: " + (stan == true ? "ON" : "OFF"));
        } catch (Exception e) {
            logger.error("Błąd podczas zmieniania stanu światła! ", e);
            return new Response<>("", e.getMessage());
        }



    }

    @GetMapping("/changeBlindState")
    public Response<String> zmienStanRolety(@RequestParam("name") String nazwaPokoju, @RequestParam("idUrzadzenia") int idUrzadzenia, @RequestParam("pozycja") boolean pozycja) {
        logger.debug("Zmien Stan Rolety w pokoju: "+ nazwaPokoju+ "; id Rolety: " + idUrzadzenia + "; do stanu: " +(pozycja ? "UP":"DOWN"));
        try {
            Device d =system.changeBlindState( nazwaPokoju, idUrzadzenia, pozycja);
            return new Response<>("Zmieniono stan Rolety :" +((Blind)d).toString()+" na pozycje: " + (((Blind)d).getStan() == RoletaStan.UP ? "UP" : "DOWN"));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
    }
    @GetMapping("/checkReinitBoard")
    public Response<String> sprawdzZainicjowaniePlytki(@RequestParam("boardID") int boardID) {
        boolean tmp = system.checkInitOfBoard(boardID);
        return new Response<String>("Sprawdzono, czy urządzenie było inicjowane i: " + (tmp?"reinicjalizowano je" : "nie było potrzeby ponownej reinicjalizacji"));
    }
    @GetMapping("/reinitBoard")
    public Response<String> reainicjowaniePlytki(@RequestParam("boardID") int boardID) {
        boolean tmp = system.initOfBoard(boardID);
        return new Response<String>("Sprawdzono, czy urządzenie było inicjowane i: " + (tmp?"reinicjalizowano je" : "nie było potrzeby ponownej reinicjalizacji"));
    }


    @GetMapping("/addPrzycisk")
    public Response<String> dodajPrzycisk(@RequestParam("name") String nazwaPokoju, @RequestParam("boardID") int boardID, @RequestParam("pin") int pin) {
        try {
            Button b = system.addButton(nazwaPokoju, boardID, pin);
            if (b != null)
                return new Response<>("Przycisk: '" + b.toString() + "' dodana prawidłowo");
            else
                return new Response<>("",
                        "Nie udało dodać się Przycisku. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania przycisku", e);
            return new Response<>(null, e.getMessage());
        }

    }
















    @RequestMapping("/shutdown")
    public Response<String> shutdownMe() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("sudo shutdown -h now");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response<>("ShuttingDownSystem");
    }

}
