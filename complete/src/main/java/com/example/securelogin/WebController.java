package com.example.securelogin;

import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import java.util.LinkedList;
import java.util.List;

import java.sql.Connection;


@Controller
public class WebController implements WebMvcConfigurer {

	private final String NOT_AUTHENTICATED = "NOT_AUTHENTICATED";

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("index");
		registry.addViewController("/index").setViewName("index");
		registry.addViewController("/register").setViewName("register");
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/profile").setViewName("profile");
		registry.addViewController("/contacts").setViewName("contacts");
	}

	private String getActualAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// Verifica se l'utente è autenticato correttamente
		if (authentication != null && authentication.isAuthenticated()) {
			// Ottieni il nome utente dall'oggetto principale dell'autenticazione
			Object principal = authentication.getPrincipal();
			String username = (principal instanceof UserDB) ? ((UserDetails) principal).getUsername() : NOT_AUTHENTICATED;
			if (username.equals(NOT_AUTHENTICATED)) {
				return NOT_AUTHENTICATED;
			}
			return username;
		} else {
			return NOT_AUTHENTICATED;
		}
	}

	@GetMapping("/profile")
	public String showProfile(Model model) {
		String actualUser = getActualAuthenticatedUser();

		if (!actualUser.equals(NOT_AUTHENTICATED)) {
			try {
				Connection c = DbUtility.createConnection();
				User user = DbUtility.getUser(c, actualUser);
				// Aggiungi l'utente al modello
				model.addAttribute("user", user);
				return "profile";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "login";
	}

	@GetMapping("/contacts")
	public String showContacts(Model model) {
		String actualUser = getActualAuthenticatedUser();

		if (!actualUser.equals(NOT_AUTHENTICATED)) {
			try {
				Connection c = DbUtility.createConnection();
				List<User> users = DbUtility.getAllUsers(c);
				model.addAttribute("users", users);
				return "contacts";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "login";
	}

	@PostMapping("/updateUser")
	public String updateUser(@ModelAttribute User user, Model model, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "profile?error";
		}

		String actualUser = getActualAuthenticatedUser();
		if (!actualUser.equals(NOT_AUTHENTICATED)) {
			try {
				Connection c = DbUtility.createConnection();
				User dbUser = DbUtility.getUser(c, actualUser);
				DbUtility.updateUserPreparedStatement(c, user, dbUser);
				DbUtility.closeConnection(c);
				return "profile";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "profile?error";
	}

	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String checkRegistrationInfo(@Valid @ModelAttribute User user, Model model, BindingResult bindingResult) {

		boolean registrationDone = false;
		if (bindingResult.hasErrors()) {
			return "register";
		}
		try {
			String salt = BCrypt.gensalt();
			Connection c = DbUtility.createConnection();
			if (DbUtility.checkIfUserIsPresent(c, user.getUsername())) {
				model.addAttribute("registrationError", "Username già presente");
				return "register";
			}
			DbUtility.insertUserPreparedStatement(c,
					user.getUsername(),
					CryptoPassword.cryptoPasswordwithSalt(user.getPassword(), salt),
					salt,
					user.getName(),
					user.getSurname());
			DbUtility.closeConnection(c);
			registrationDone=true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(registrationDone) {
			model.addAttribute("user", user);
			return "redirect:index";
		} else {
			model.addAttribute("registrationError", "Errore durante la registrazione dell'utente");
			return "register";
		}
	}

	@GetMapping("/login")
	public String showForm(Model model) {
		model.addAttribute("user", new User());
		return "login";
	}

	@PostMapping("/login")
	public String checkPersonInfo(@Valid @ModelAttribute User user, Model model, BindingResult bindingResult) {

		boolean userLogged = false;
		if (bindingResult.hasErrors()) {
			return "login";
		}
        try {
            Connection c = DbUtility.createConnection();
			if (DbUtility.checkIfUserIsPresent(c, user.getUsername())) {
				System.out.println("Found user " + user.getUsername());
				User dbUser = DbUtility.getUser(c, user.getUsername());
				DbUtility.closeConnection(c);
				if (CryptoPassword.cryptoPasswordwithSalt(user.getPassword(), dbUser.getSalt()).equals(dbUser.getPassword())) {
					System.out.println("Right user's password");
					userLogged=true;
					user.setSalt(dbUser.getSalt());
					user.setName(dbUser.getName());
					user.setSurname(dbUser.getSurname());
					user.setTelephone(dbUser.getTelephone());
				} else {
					System.out.println("Wrong user's password");
					model.addAttribute("loginError", "Username/password errati");
					return "login";
				}
			} else {
				System.out.println("User " + user.getUsername() + " not found");
				model.addAttribute("loginError", "Username/password errati");
				return "login";
			}
		} catch (Exception e) {
            e.printStackTrace();
        }

		if(userLogged) {
			model.addAttribute("logged", userLogged);
			System.out.println("User logged, redirect to index");
			//model.addAttribute("user", user);
			return "index";
		} else {
			System.out.println("Error, redirect to login");
			model.addAttribute("loginError", "Errore durante il login...");
			return "login";
		}
	}
}
