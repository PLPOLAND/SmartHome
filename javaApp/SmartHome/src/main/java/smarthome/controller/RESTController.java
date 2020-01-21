package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smarthome.database.UsersDAO;
import smarthome.security.Security;

/**
 * RestController
 */
@RestController
@RequestMapping("/api")
public class RESTController{
    @Autowired
    UsersDAO users;

    @RequestMapping("/login")
    boolean login(HttpServletRequest request){
        Security s = new Security(request, users);
        if (s.login())
            return true;
        else
            return false;
    }
    
}