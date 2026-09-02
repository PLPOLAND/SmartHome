package newsmarthome.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import newsmarthome.automation.Function;
import newsmarthome.database.AutomationDAO;
import newsmarthome.database.SystemDAO;
import newsmarthome.database.UsersDAO;
import newsmarthome.exception.HardwareException;
import newsmarthome.model.response.Response;
import newsmarthome.security.MobileSecurity;

@RestController
@RequestMapping("/api")
public class AutomationController {

    final UsersDAO usersDAO;
    final SystemDAO systemDAO;
    final AutomationDAO automationDAO;

    Logger logger = LoggerFactory.getLogger(AutomationController.class);

    AutomationController(@Autowired UsersDAO usersDAO, @Autowired SystemDAO systemDAO,
            @Autowired AutomationDAO automationDAO) {
        this.usersDAO = usersDAO;
        this.systemDAO = systemDAO;
        this.automationDAO = automationDAO;
    }

    @GetMapping("/getAutomations")
    public Response<List<Function>> getAutomations(HttpServletRequest request) {
        MobileSecurity security = new MobileSecurity(request, usersDAO);
        if (!security.isLoged()) {
            return new Response<>(null, "Użytkownik nie jest zalogowany");
        } else {
            return new Response<>(automationDAO.getAllFunctionsList());
        }
    }

    @PostMapping("/runAutomation")
    public Response<String> runAutomation(HttpServletRequest request, @RequestParam int id) {
        MobileSecurity security = new MobileSecurity(request, usersDAO);
        if (!security.isLoged()) {
            return new Response<>(null, "Użytkownik nie jest zalogowany");
        } else {
            try {
                Function function = automationDAO.getFunction(id);
            
                if (function == null) {
                    return new Response<>(null, "Nie znaleziono funkcji");
                } else {
                        function.run();
                    return new Response<>("Uruchomiono funkcję");
                }
            } catch (HardwareException e) {
                return new Response<>(null, e.getMessage());
            }catch (IllegalArgumentException e) {
                return new Response<>(null, "Nie znaleziono funkcji: " + e.getMessage());
            }
        }
    }
    @GetMapping("/getAutomationState")
    public Response<Boolean> getAutomationState(HttpServletRequest request, @RequestParam int id) {
        MobileSecurity security = new MobileSecurity(request, usersDAO);
        if (!security.isLoged()) {
            return new Response<>(null, "Użytkownik nie jest zalogowany");
        } else {
            try {
                Function function = automationDAO.getFunction(id);
            
                if (function == null) {
                    return new Response<>(null, "Nie znaleziono funkcji");
                } else {
                    return new Response<>(function.isActive());
                }
            }catch (IllegalArgumentException e) {
                return new Response<>(null, "Nie znaleziono funkcji: " + e.getMessage());
            }
        }
    }



    // @PostMapping("/addAutomation")
    // public void addAutomation(Function function) {
    // automationDAO.addFunction(function);
    // }

}
