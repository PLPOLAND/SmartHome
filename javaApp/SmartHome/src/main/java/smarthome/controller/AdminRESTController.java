package smarthome.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.pi4j.io.i2c.I2CDevice;

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
import smarthome.i2c.MasterToSlaveConverter;
import smarthome.model.Response;
import smarthome.model.Room;
import smarthome.model.Uprawnienia;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.DeviceTypes;
import smarthome.model.hardware.Light;
import smarthome.model.hardware.Sensor;
import smarthome.model.hardware.SensorsTypes;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.ButtonFunction;
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
    MasterToSlaveConverter converter;


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
        if (s.login()){
            if (request.getParameter("link").equals("")) {
                return new Response<>("/admin/");
                
            } else {
                
                return new Response<>(request.getParameter("link"));
            }
        }
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

    @RequestMapping("/getTmpTermometr")
    public Response<ArrayList<Termometr>> getTMPTermometrs() {
        ArrayList<Termometr> tmp = new ArrayList<>();
        tmp.add(new Termometr(100, systemDAO.getRoom("Brak").getID(), 0, new int[]{0,0,0,0,0,0,0,0}, 25.F, 30.F, 0.F));
        return new Response<>(tmp);
    }

    @RequestMapping("/getSystemData")
    public Response<SystemDAO> getSystemData() {
        return new Response<>(systemDAO);
    }
    @RequestMapping("/getDeviceTypes")
    public Response<String[]> getDeviceTypes() {
        return new Response<>(DeviceTypes.getNames());
    }
    @RequestMapping("/getDevices")
    public Response<ArrayList<Device>> getDevices() {
        return new Response<>(systemDAO.getDevices());
    }
    @RequestMapping("/getDeviceById")
    public Response<Device> getDeviceById(@RequestParam("id") int id) {
        return new Response<>(system.getDeviceByID(id));
    }
    @RequestMapping("/getSensorById")
    public Response<Sensor> getSensorById(@RequestParam("id") int id) {
        return new Response<>(system.getSensorByID(id));
    }
    @RequestMapping("/getSensors")
    public Response<ArrayList<Sensor>> getSensors() {
        return new Response<>(systemDAO.getSensors());
    }
    @RequestMapping("/getTermometers")
    public Response<ArrayList<Termometr>> getThermometers() {
        return new Response<>(systemDAO.getAllTermometers());
    }
    
    @RequestMapping("/getSensorTypes")
    public Response<String[]> getSensorTypes() {
        return new Response<>(SensorsTypes.getNames());
    }

    @RequestMapping("/getRoomsNamesList")
    public Response<ArrayList<String>> getRoomsNameList() {
        
        return new Response<>(systemDAO.getRoomsNames());
    }
    @RequestMapping("/getRoomsList")
    public Response<ArrayList<Room>> getRoomsList() {
        
        return new Response<>(systemDAO.getRoomsArrayList());
    }
    @RequestMapping("/getLightList")
    public Response<ArrayList<Light>> getLightList() {
        ArrayList<Light> lights = new ArrayList<>();
        for (Device device : systemDAO.getDevices()) {
            if(device.getTyp()==DeviceTypes.LIGHT){
                lights.add((Light) device);
            }
        }
        return new Response<>(lights);
    }
    @RequestMapping("/getBlindsList")
    public Response<ArrayList<Blind>> getBlindsList() {
        ArrayList<Blind> blinds = new ArrayList<>();
        for (Device device : systemDAO.getDevices()) {
            if(device.getTyp()==DeviceTypes.BLIND){
                blinds.add((Blind) device);
            }
        }
        return new Response<>(blinds);
    }
    @RequestMapping("/getButtonFunction")
    public Response<List<ButtonFunction>> getButtonFunctions(@RequestParam("buttonId")int buttonId) {
        Button b= (Button)system.getSensorByID(buttonId);
        return new Response<>(b.getFunkcjeKlikniec());
    }

    @GetMapping("/addRoom")
    public Response<String> dodajPokoj(@RequestParam("name") String name){
        Room r = new Room(systemDAO.getRoomsArrayList().size(), name);
        systemDAO.addRoom(r);

        return new Response<>("Pokój: '" + name +"' dodany");
    }
    @GetMapping("/editRoom")
    public Response<String> edytujPokoj(@RequestParam("name") String name, @RequestParam("originalName") String oldName){
        Room r = systemDAO.getRoom(oldName);
        if (r != null) {
            r.setNazwa(name);    
            systemDAO.save(r);
        }
        else{
            return new Response<>(null,"Błędna nazwa pokoju! Taki pokój nie istnieje!");
        }
        return new Response<>("Nazwa pokoju: '" + oldName +"' została zmieniona na: '"+name+"'");
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
    public Response<String> dodajSwiatlo(@RequestParam("roomName") String nazwaPokoju, @RequestParam("name") String deviceName,@RequestParam("boardID") int boardID, @RequestParam("pin") int pin){
        
        try {
            Light l = (Light) system.addLight(nazwaPokoju, deviceName, boardID, pin);
            if (l != null)
                return new Response<String>("Żarówka: '" + l.toString() + "' dodana prawidłowo");
            else
                return new Response<String>("","Nie udało dodać się Żarówki. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania światła",e);
            return new Response<String>(null, e.getMessage());
        }
        

    }

    @GetMapping("/editSwiatlo")
    public Response<String> editSwiatlo(@RequestParam("deviceId") int deviceId,@RequestParam("roomName") String roomName, @RequestParam("name") String deviceName,@RequestParam("boardID") int boardID, @RequestParam("pin") int pin){
        
        try {
            Light l = (Light) system.getDeviceByID(deviceId);
            if (l != null){//TODO move to system
                l.setName(deviceName);
                l.setSlaveID(boardID);
                l.setPin(pin);

                systemDAO.getRoom(l.getRoom()).delDevice(l);
                systemDAO.getRoom(roomName).addDevice(l);
                systemDAO.save();
                //TODO Aktualizacja na płytkach!
                return new Response<>("Żarówka: '" + l.toString() + "' uaktualniona prawidłowo");
            }
            else
                return new Response<>("","Nie udało znaleźć się Żarówki. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania światła",e);
            return new Response<>(null, e.getMessage());
        }
    }
    @GetMapping("/editRoleta")
    public Response<String> editRoleta(@RequestParam("deviceId") int deviceId,@RequestParam("roomName") String roomName, @RequestParam("name") String deviceName,@RequestParam("boardID") int boardID, @RequestParam("pin") int pin, @RequestParam("pinDown") int pinDown){
        
        try {
            Blind l = (Blind) system.getDeviceByID(deviceId);
            if (l != null){//TODO move to System
                l.setName(deviceName);
                l.setSlaveID(boardID);
                l.setPinUp(pin);
                l.setPinDown(pinDown);
                systemDAO.getRoom(l.getRoom()).delDevice(l);
                systemDAO.getRoom(roomName).addDevice(l);
                systemDAO.save();
                //TODO Aktualizacja na płytkach!
                return new Response<>("Roleta: '" + l.toString() + "' uaktualniona prawidłowo");
            }
            else
                return new Response<>("","Nie udało znaleźć się Rolety. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania światła",e);
            return new Response<>(null, e.getMessage());
        }
    }
    @GetMapping("/addRoleta")
    public Response<String> dodajRoleta(@RequestParam("roomName") String nazwaPokoju,@RequestParam("name") String deviceName,@RequestParam("boardID") int boardID, @RequestParam("pin") int pinUp, @RequestParam("pinDown") int pinDown){
        
        
        try {
            Blind l = (Blind) system.addRoleta(nazwaPokoju, deviceName, boardID, pinUp, pinDown);
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
        Room r = systemDAO.getRoom(nazwaPokoju);
        try {
            if((dev = r.getDeviceById(id)) == null)
                throw new NullPointerException("Brak urzadzenia o id: "+id+" w pokoju: "+nazwaPokoju);
            
            system.removeDevice(dev, r);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
        return new Response<>("Urzadzenie: '" + dev.toString() + "' usnięte prawidłowo z pokoju: " + nazwaPokoju);
    }
    @GetMapping("/removeDeviceByID")

    public Response<String> removeDeviceByID(@RequestParam("id") int id){
        
        Device dev = null;
        Room r = null;
        try {
            if((dev = system.getDeviceByID(id)) != null){
                r = systemDAO.getRoom(dev.getRoom());
                system.removeDevice(dev, r);
                return new Response<>("Urzadzenie: '" + dev.toString() + "' usnięte prawidłowo z systemu");
            }
            else{
                return new Response<>("", "Nie znaleziono Urządzenia o podanym ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
    }
    @GetMapping("/removeSensorByID")

    public Response<String> removeSensorByID(@RequestParam("id") int id){
        
        Sensor sen = null;
        Room r = null;
        try {
            if((sen = system.getSensorByID(id)) != null){
                r = systemDAO.getRoom(sen.getRoom());
                system.removeSensor(sen, r);
                return new Response<>("Sensor: '" + sen.toString() + "' usnięte prawidłowo z systemu");
            }
            else{
                return new Response<>("", "Nie znaleziono Urządzenia o podanym ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
    }

    @GetMapping("/removeRoom")
    public Response<String> removeRoom(@RequestParam("name") String nazwaPokoju) {
        Room room = null;
        try {
            if ((room = systemDAO.getRoom(nazwaPokoju)) == null)
                throw new IllegalArgumentException("Brak pokoju o nazwie: "+ nazwaPokoju);
            systemDAO.removeRoom(room);
            logger.warn("Pokój '{}' został usunięty z systemu!",nazwaPokoju);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
        return new Response<>("Pokój: '" + room.getNazwa() + "' usnięty prawidłowo");
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
    @GetMapping("/addButtonClickFunction")
    public Response<String> addButtonClickFunction(@RequestParam("buttonID") int buttonID,@RequestParam("deviceID") int deviceId, @RequestParam("state") ButtonFunction.State state, @RequestParam("clicks") int clicks ) {
        try {
            Device device = system.getDeviceByID(deviceId);
            system.addFunctionToButton(buttonID, device, state, clicks);
            return new Response<>("Funkcja przycisku o id: "+buttonID+" dodana pomyślnie");
        } catch (Exception e) {
            logger.error("Bład podczas dodawania funkcji kliknięć do przycisku", e);
            return new Response<>("", e.getMessage());
        }
        
        
    }
    @GetMapping("/rmButtonClickFunction")
    public Response<String> rmButtonClickFunction(@RequestParam("buttonID") int buttonID, @RequestParam("clicks") int clicks ) {
        try {
            system.removeFunctionToButton(buttonID,clicks);
            return new Response<>("Funkcja przycisku o id: "+buttonID+" usunieta pomyślnie");
        } catch (Exception e) {
            logger.error("Bład podczas dodawania funkcji kliknięć do przycisku", e);
            return new Response<>("", e.getMessage());
        }
        
        
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
    @PostMapping("/changeBlindStateByRoomID")
    public Response<String> zmienStanRoletyByRoomID(@RequestParam("roomID") int roomID, @RequestParam("idUrzadzenia") int idUrzadzenia, @RequestParam("pozycja") boolean pozycja) {

        Room r = systemDAO.getRoom(roomID);
        
        logger.debug("Zmien Stan Rolety w pokoju: "+ r.getNazwa()+ "; id Rolety: " + idUrzadzenia + "; do stanu: " +(pozycja ? "UP":"DOWN"));
        try {
            Device d =system.changeBlindState( r.getNazwa(), idUrzadzenia, pozycja);
            return new Response<>("Zmieniono stan Rolety :" +((Blind)d).toString()+" na pozycje: " + (((Blind)d).getStan() == RoletaStan.UP ? "UP" : "DOWN"));
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("", e.getMessage());
        }
    }
    @GetMapping("/checkReinitBoard")
    public Response<String> sprawdzZainicjowaniePlytki(@RequestParam("boardID") int boardID) {
        try {
            boolean tmp = system.checkInitOfBoard(boardID);
            return new Response<>("Sprawdzono, czy urządzenie było inicjowane i: " + (tmp?"reinicjalizowano je" : "nie było potrzeby ponownej reinicjalizacji"));
        } catch (Exception e) {
            logger.error("Błąd podczas sprawdzania czy plytka była zainicjowana", e);
            return new Response<>("", e.getMessage());
        }
    }
    @GetMapping("/reinitBoard")
    public Response<String> reainicjowaniePlytki(@RequestParam("boardID") int boardID) {
        boolean tmp = system.initOfBoard(boardID);
        return new Response<>("Sprawdzono, czy urządzenie było inicjowane i: " + (tmp?"reinicjalizowano je" : "nie było potrzeby ponownej reinicjalizacji"));
    }


    @GetMapping("/addPrzycisk")
    public Response<String> dodajPrzycisk(@RequestParam("name") String nazwa,@RequestParam("roomName") String nazwaPokoju, @RequestParam("boardID") int boardID, @RequestParam("pin") int pin) {
        try {
            Button b = system.addButton(nazwa,nazwaPokoju, boardID, pin);
            if (b != null)
            {
                return new Response<>("Przycisk: '" + b.toString() + "' dodana prawidłowo");
            }else
                return new Response<>("",
                        "Nie udało dodać się Przycisku. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas dodawania przycisku", e);
            return new Response<>(null, e.getMessage());
        }

    }

    @GetMapping("/editButton")
    public Response<String> editButton(@RequestParam("buttonId") int buttonId,@RequestParam("roomName") String roomName, @RequestParam("name") String buttonName,@RequestParam("boardID") int newSlaveID, @RequestParam("pin") int pin){
        
        try {
            Button b = (Button) system.getSensorByID(buttonId);
            if (b != null){//TODO move to System
                b.setName(buttonName);
                int oldSlaveID = b.getSlaveAdress();
                b.setSlaveID(newSlaveID);
                b.setPin(pin);
                systemDAO.getRoom(b.getRoom()).delSensor(b);
                systemDAO.getRoom(roomName).addSensor(b);
                systemDAO.save();
                system.initOfBoard(oldSlaveID);
                system.initOfBoard(newSlaveID);
                return new Response<>("Przycisk: '" + b.getName() + "' uaktualnione prawidłowo");
            }
            else
                return new Response<>("","Nie udało znaleźć się Przycisku o id: '"+buttonId+"'. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas uaktualniania Przycisku",e);
            return new Response<>(null, e.getMessage());
        }
    }
    @GetMapping("/editThermometer")
    public Response<String> editThermometer(@RequestParam("thermometerId") int thermometerId,@RequestParam("roomName") String roomName, @RequestParam("name") String thermometerName){
        
        try {
            system.editThermometer(thermometerId, thermometerName, roomName);
            return new Response<>("Termometr '" +thermometerName + "'został zaktualizowany poprawnie");
            } catch (Exception e) {
            logger.error("Błąd podczas uaktualniania Przycisku",e);
            return new Response<>(null, e.getMessage());
        }
    }

    @GetMapping("/editButtonFunction")
    public Response<String> editButtonFunction(@RequestParam("buttonId") int buttonId,@RequestParam("deviceId") int deviceId, @RequestParam("state") ButtonFunction.State state, @RequestParam("clicks") int clicks,@RequestParam("oldClicks") int oldclicks ) {

        try {
            Button b = (Button) system.getSensorByID(buttonId);
            if (b != null) {// TODO move to System
                 Device device = system.getDeviceByID(deviceId);
                system.removeFunctionFromButton(b, oldclicks);
                system.addFunctionToButton(buttonId, device, state, clicks);
                return new Response<>("Funkcja uaktualniona prawidłowo");
            } else
                return new Response<>("", "Nie udało znaleźć się Przycisku o id: '" + buttonId
                        + "'. Sprawdź konsolę programu w poszukiwaniu szczegółów");
        } catch (Exception e) {
            logger.error("Błąd podczas uaktualniania Przycisku", e);
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
