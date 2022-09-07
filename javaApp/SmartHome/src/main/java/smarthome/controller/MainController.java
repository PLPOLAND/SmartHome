package smarthome.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


import smarthome.database.UsersDAO;
import smarthome.security.Security;

@Controller
public class MainController {

	@Autowired
	UsersDAO users;

	@RequestMapping("/")
	public String mainpage(HttpServletRequest request){
		Security sec = new Security(request, users);
		if(!sec.isLoged())
			return "redirect:login";

		return "index";
	}

	@RequestMapping("/userSetings")
	public String userSetings(HttpServletRequest request, Model model) {
		Security sec = new Security(request, users);
		if (!sec.isLoged())
			return "redirect:login?l=/userSetings";
		model.addAttribute("userID", sec.getUserID());
		model.addAttribute("admin", false);
		return "admin/editUser";
	}

	@RequestMapping("/login")
	public String loadLoginPage() {
		return "loginPage.html";
	}

	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) {
		Security sec = new Security(request, users);
		sec.logout();
		return "redirect:/";
	}

	@RequestMapping("/tmp")
	public String tmp(){
		return "tmp.html";
	}

}
