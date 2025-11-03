package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Displays the application's home page.
     *
     * This is the landing page of the application, accessible to all users
     * regardless of authentication status.
     *
     *
     * @return the name of the view template "index"
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Displays the login page.
     *
     * This page allows users to authenticate and access protected resources
     * within the application.
     *
     *
     * @return the name of the view template "login"
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Displays the signup page.
     *
     * This page provides a registration form for new users to create an account.
     *
     *
     * @return the name of the view template "signup"
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    /**
     * Processes user registration.
     *
     * This method validates that the username is unique, creates a new user
     * account with the provided credentials, and redirects to the login page
     * upon successful registration. If the username already exists or registration
     * fails, an error message is displayed on the signup page.
     *
     *
     * @param username the desired username for the new account
     * @param password the password for the new account
     * @param email the optional email address for the new account
     * @param model the Spring MVC model for passing data to the view
     * @return a redirect to the login page with a success parameter if registration is successful
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           Model model) {
        try {
            // Check if user already exists
            if (userService.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Username already exists");
                return "signup";
            }

            userService.registerUser(username, password);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed");
            return "signup";
        }
    }

    /**
     * Displays the authenticated user's profile page.
     *
     * This method retrieves the currently authenticated user's information
     * and adds it to the model for display. If no user is authenticated,
     * the profile page will be displayed without user data.
     *
     *
     * AuthenticationPrincipal the Spring Security principal representing the authenticated user,
     *                  or null if no user is authenticated
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "profile"
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                          Model model) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("user", user);
        }
        return "profile";
    }
}