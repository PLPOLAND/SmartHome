package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import smarthome.database.UsersDAO;
import smarthome.security.Security;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UsersDAO users;
    @RequestMapping("")
    public String admin(){
        return "redirect:/admin/";
    }
    @RequestMapping("/")
    public String adminHome(HttpServletRequest request){
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return "redirect:login";

        return "admin/index";
    }

    @RequestMapping("/login")
    public String adminLogin(HttpServletRequest request) {
        Security sec = new Security(request, users);
        if (sec.isLoged() && sec.isUserAdmin())
            return "redirect:/admin/";

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
            return "redirect:login";

        return "admin/addRoom";
    }

}
