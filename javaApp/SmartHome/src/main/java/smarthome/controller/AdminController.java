package smarthome.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import smarthome.database.UsersDAO;
import smarthome.model.hardware.Blind;
import smarthome.model.hardware.Button;
import smarthome.model.hardware.Device;
import smarthome.model.hardware.Light;
import smarthome.security.Security;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UsersDAO users;

    @Autowired
    smarthome.system.System system;

    @Autowired
    AdminRESTController adminRESTController;

    @RequestMapping("")
    public String admin(){
        return "redirect:/admin/";
    }
    @RequestMapping("/")
    public String adminHome(HttpServletRequest request){
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        return "admin/index";
    }

    @RequestMapping("/login")
    public String adminLogin(HttpServletRequest request, @RequestParam(name = "l", defaultValue = "") String l, Model model) {
        Security sec = new Security(request, users);
        if (sec.isLoged() && sec.isUserAdmin())
            return "redirect:" + l;

        model.addAttribute("link", l);
        return "loginPage";
    }

    @RequestMapping("/logout")
    String logout(HttpServletRequest request) {
        Security s = new Security(request, users);
        s.logout();
        return "redirect:/admin/login";
    }

    @RequestMapping("/addRoom")
    public String addRoom(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/addRoom";
    }
    @RequestMapping("/removeRoom")
    public String rmRoom(@RequestParam(name = "roomName", required = false, defaultValue = "")String roomName,HttpServletRequest request, Model model) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        model.addAttribute("roomName", roomName);
        return "admin/rmRoom";
    }

    @RequestMapping("/addDevice")
    public String addDevice(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/addDevice";
    }
    @RequestMapping("/addSensor")
    public String addSensor(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/addSensor";
    }
    @RequestMapping("/roomsList")
    public String roomList(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/roomsList";
    }
    @RequestMapping("/listOfDevices")
    public String listOfDevices(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/deviceList";
    }
    @RequestMapping("/listOfSensors")
    public String listOfSensors(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);

        return "admin/sensorList";
    }
    @RequestMapping("/editRoom")
    public String editRoom(@RequestParam("roomName")String roomName, HttpServletRequest request, Model model) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        model.addAttribute("roomName", roomName);
        return "admin/editRoom";
    }

    @RequestMapping("/editDevice")
    public String editDevice(@RequestParam("deviceID") int deviceID, HttpServletRequest request, Model model) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        Device tmp = adminRESTController.system.getDeviceByID(deviceID);
        model.addAttribute("deviceID", deviceID);
        model.addAttribute("deviceName", tmp.getName());
        model.addAttribute("slave", tmp.getSlaveID());
        
        if (tmp instanceof Blind) {
            model.addAttribute("pin1", ((Blind) tmp).getPinUp());
            model.addAttribute("pin2", ((Blind) tmp).getPinDown());
        }
        else if (tmp instanceof Light ) {//TODO Dodać gniazdko
            model.addAttribute("pin1", ((Light)tmp).getPin());
        }
        
        return "admin/editDevice";
    }
   
    @RequestMapping("/editButton")
    public String editButton(@RequestParam(name = "id",defaultValue = "0") int buttonID, HttpServletRequest request, Model model) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        Button tmp = (Button) system.getSensorByID(buttonID);
        model.addAttribute("slaveID", tmp.getSlaveID());
        model.addAttribute("buttonName", tmp.getName());
        model.addAttribute("slave", tmp.getSlaveID());
        model.addAttribute("pin", tmp.getPin());
        model.addAttribute("buttonID", buttonID);

        return "admin/editButton";
    }



    @RequestMapping("/shutdown")
    public String shutdown(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return this.reredairect(request);
        adminRESTController.shutdownMe();
        return "admin/shutdown";
    }

    String reredairect(HttpServletRequest request){
        String str = "redirect:login?l=";
        str += request.getRequestURI();
        str += "?";
        Map<String,String[]> tmp = request.getParameterMap();
        for (String name : tmp.keySet()) {
            for (String val : tmp.get(name)) {
                str +=name + "=";
                str += val;
                str +="&";
            }
        }
        return str.substring(0, str.length()-1);
    }
}
