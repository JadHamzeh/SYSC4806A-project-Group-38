package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.List;

/**
 * The controller for handling page loading for the home, login, signup & profile page.
 * Processes data when the user signs up on the signup page.
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private UserMembershipService userMembershipService;

    /**
     * Displays the application's home page.
     *
     * This is the landing page of the application, accessible to all users
     * regardless of authentication status.
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
     * @param username the desired username for the new account
     * @param password the password for the new account
     * @param email    the optional email address for the new account
     * @param model    the Spring MVC model for passing data to the view
     * @return a redirect to the login page with a success parameter if registration is successful
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           Model model,
                           jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Check if user already exists
            if (userService.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Username already exists");
                return "signup";
            }

            userService.registerUser(username, password);
            request.login(username, password);

            return "redirect:/perks/dashboard";
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
     * AuthenticationPrincipal the Spring Security principal representing the authenticated user, or null if no user is authenticated.
     *
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "profile"
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                          Model model) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("user", user);

            List<MembershipType> allMemberships = membershipService.getAllMemberships();
            model.addAttribute("allMemberships", allMemberships);

            List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);
            model.addAttribute("userMemberships", userMemberships);
        }
        return "profile";
    }

    /**
     * Adds a membership type to the currently authenticated user's profile.
     *
     * The method retrieves the logged-in user, assigns the selected membership type,
     * reloads updated membership data, and returns a fragment for partial page updates.
     *
     * @param membershipTypeId the ID of the membership type to add
     * @param principal the authenticated user from the security context
     * @param model the model used to pass updated user and membership data to the view
     * @return the membership list fragment to re-render on the client
     */
    @PostMapping("/profile/add-membership")
    public String addMembership(@RequestParam Long membershipTypeId,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                Model model) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            MembershipType membershipType = membershipService.findById(membershipTypeId).orElse(null);

            if (user != null && membershipType != null) {
                userMembershipService.assignMembership(user, membershipType);

            }

            user = userService.findById(user.getId()).orElse(null);
            List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);

            model.addAttribute("user", user);
            model.addAttribute("userMemberships", userMembershipService.getMembershipsForUser(user));
            model.addAttribute("allMemberships", membershipService.getAllMemberships());
        }

        return "fragments/membership-list :: membership-section";
    }

    /**
     * Removes a membership type from the currently authenticated user's profile.
     *
     * The method looks up the user, removes the selected membership type if present,
     * reloads updated membership information, and returns a fragment for page updates.
     *
     * @param membershipTypeId the ID of the membership type to remove
     * @param principal the authenticated user from the security context
     * @param model the model used to pass updated user and membership data to the view
     * @return the membership list fragment to re-render on the client
     */
    @PostMapping("/profile/remove-membership")
    public String removeMembership(@RequestParam Long membershipTypeId,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                Model model) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);

            if (user != null) {
                userMembershipService.removeMembershipByUserAndType(user.getId(), membershipTypeId);

            }

            user = userService.findById(user.getId()).orElse(null);
            List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);

            model.addAttribute("user", user);
            model.addAttribute("userMemberships", userMembershipService.getMembershipsForUser(user));
            model.addAttribute("allMemberships", membershipService.getAllMemberships());
        }

        return "fragments/membership-list :: membership-section";
    }
}