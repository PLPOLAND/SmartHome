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
    
    @RequestMapping("/main")
    public String adminHome(HttpServletRequest request){
        Security sec = new Security(request, users);
        if (!sec.isLoged() || !sec.isUserAdmin())
            return "redirect:login";

        return "index";
    }


}
