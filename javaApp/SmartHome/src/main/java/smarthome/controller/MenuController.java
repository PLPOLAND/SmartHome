package smarthome.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smarthome.database.SystemDAO;
import smarthome.database.UsersDAO;
import smarthome.menu.Menu;
import smarthome.menu.PozycjaMenu;
import smarthome.model.Response;
import smarthome.security.Security;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    @Autowired
    UsersDAO users;
    @Autowired
    SystemDAO system;
    
    @RequestMapping("/pozycje")
    public Response<ArrayList<PozycjaMenu>> getpozycje(HttpServletRequest request) {
        Security s = new Security(request, users);
        if(s.isLoged())
        return new Response<ArrayList<PozycjaMenu>>(new Menu(s.getFullUserData(),system).getPozycje());
        else
        return new Response<ArrayList<PozycjaMenu>>(null,"Nie zalogowano!");
    }
}
