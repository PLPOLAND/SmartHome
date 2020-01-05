package smarthome.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller
 */
@Controller
public class MainController {

    @RequestMapping("/")
    public String loadLoginPage() {
        return "mainpage";
    }
}