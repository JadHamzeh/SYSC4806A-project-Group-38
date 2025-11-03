package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/user/memberships")
public class MembershipController {

    @Autowired
    private UserMembershipService userMembershipService;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    @Autowired
    private UserMembershipRepository userMembershipRepository;

    /**
     * Displays a list of all memberships available in the system along with
     * the current user's memberships.
     *
     * This method retrieves both the memberships that the authenticated user
     * currently has and all available membership types in the system, then
     * adds them to the model for display.
     *
     * @param user the authenticated user
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "user-memberships"
     */
    @GetMapping("/list")
    public String ListMemberships(@AuthenticationPrincipal User user, Model model){
        List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);

        List<MembershipType> Memberships = membershipTypeRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("usermemberships", userMemberships);
        model.addAttribute("Memberships", Memberships);

        return "user-memberships";


    }

    /**
     * Displays the form for adding a new membership to the user's account.
     *
     * This method filters out memberships that the user already has and provides
     * a list of available memberships that can be added.
     *
     *
     * @param user the authenticated user
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "add-membership"
     */

    @GetMapping("/new")
    public String addNewMembership(@AuthenticationPrincipal User user, Model model){
        List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);
        List<MembershipType> availableMemberships = membershipTypeRepository.findAll().stream().filter(m -> !userMemberships.contains(m)).toList();

        model.addAttribute("availableMemberships", availableMemberships);

        return "add-membership";
    }

    /**
     * Processes the addition of a new membership to the authenticated user's account.
     * This method validates that the requested membership exists, assigns it to the user,
     * and then redirects to the membership list page.
     *
     * @param Id the ID of the membership type to add
     * @param user the authenticated user
     * @param model the Spring MVC model for passing data to the view
     * @return a redirect to the user memberships page
     */
    @PostMapping("/add")
    public String addMemberships(@RequestParam long Id, @AuthenticationPrincipal User user, Model model){
        MembershipType membershipType = membershipTypeRepository.findById(Id).orElseThrow(() -> new RuntimeException("Membership not part of available memberships"));

        userMembershipService.assignMembership(user, membershipType);

        List<MembershipType> newMembershipList = userMembershipService.getMembershipsForUser(user);

        model.addAttribute("userMemberships", newMembershipList);

        return "redirect:/user/memberships";

    }

    /**
     * Processes the removal of a membership from the authenticated user's account.
     *
     * This method validates that the user membership exists, removes it from the user's
     * account, and then redirects to the membership list page.
     *
     * @param id the ID of the user membership to remove
     * @param user the authenticated user
     * @param model the Spring MVC model for passing data to the view
     * @return a redirect to the user memberships page
     */
    @PostMapping("/remove")
    public String removeMemberships(@RequestParam long id, @AuthenticationPrincipal User user, Model model){
        UserMembership userMembership = userMembershipRepository.findById(id).orElseThrow(() -> new RuntimeException("Membership not found"));

        userMembershipService.removeMembership(userMembership);

        List<MembershipType> newMembershipList = userMembershipService.getMembershipsForUser(user);
        model.addAttribute("userMemberships", newMembershipList);

        return "redirect:/user/memberships";


    }


}
