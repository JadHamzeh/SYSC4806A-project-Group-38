package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/perks")
public class PerkController {
    @Autowired
    private PerkService perkService;

    private MembershipTypeRepository membershipTypeRepository;

    /**
     * Displays the perks dashboard page.
     * This method retrieves all available perks and determines whether a user
     * is authenticated. It adds the appropriate user information and perks
     * to the model for display on the dashboard.
     *
     * @param user the authenticated user, or null if not authenticated
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "dashboard"
     */
    @GetMapping("/dashboard")
    public String perksPage( @AuthenticationPrincipal User user, Model model) {

        if (user != null) {
            // User logged in
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentUser", user);
        } else {
            // User NOT logged in
            model.addAttribute("isLoggedIn", false);
        }

        model.addAttribute("perks", perkService.getAllPerks());
        return "dashboard";
    }

    /**
     * Searches and sorts perks based on membership type and sorting criteria.
     * <p>
     * This method retrieves perks and applies sorting based on the specified
     *
     * @param MembershipType the membership type to filter perks by
     * @param sortBy the sorting criterion: "votes", "expiry", or "relevance" (default: "votes")
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "dashboard"
     */
    @GetMapping("/dashboard")
    public String perkSearch(
            @RequestParam String MembershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            Model model){

        List<Perk> perks = perkService.getAllPerks();

        if (sortBy.equalsIgnoreCase("votes")) {
            perks.sort(Comparator.comparingInt(Perk::getVotes).reversed());
        } else if (sortBy.equalsIgnoreCase("expiry")) {
            perks.sort(Comparator.comparing(Perk::getExpiryDate));
        } else if (sortBy.equalsIgnoreCase("relevance")) {
            perkService.searchByMembership(MembershipType);
        }

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);

        return "dashboard";
    }

    /**
     * Displays the form for creating a new perk.
     *
     * This method retrieves all available membership types and adds them to
     * the model to populate a dropdown or selection list in the perk creation form.
     *
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the view template "new-perk"
     */
    @GetMapping("/new")
    public String newPerkForm(Model model) {
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "new-perk";
    }

    /**
     * Processes the creation of a new perk.
     * This method accepts perk details from a form submission, creates a new
     * perk associated with the authenticated user, and redirects to the dashboard.
     *
     * @param title the title of the perk
     * @param description the description of the perk
     * @param region the geographical region where the perk is available
     * @param membershipName the name of the membership type associated with the perk
     * @param expiryDate the date when the perk expires
     * @param user the authenticated user creating the perk
     * @param model the Spring MVC model for passing data to the view
     * @return a redirect to the perks dashboard
     */
    @PostMapping("/create")
    public String createPerk(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String region,
            @RequestParam String membershipName,
            @RequestParam LocalDate expiryDate,
            @AuthenticationPrincipal User user,
            Model model) {

        Perk createdPerk = perkService.createPerk(title, description, region, membershipName, user.getId(), expiryDate);

        model.addAttribute("perk", createdPerk);

        return "redirect:/perks/dashboard";
    }

    /**
     * Processes an upvote for a specific perk.
     *
     * This method increments the vote count for the specified perk and
     * redirects back to the dashboard.
     *
     *
     * @param perkId the ID of the perk to upvote
     * @return a redirect to the perks dashboard
     */
    @PostMapping("/{perkId}/upvote")
    public String upvotePerk(@PathVariable Long perkId) {
        perkService.vote(perkId, true);
        return "redirect:/perks/dashboard";
    }

    /**
     * Processes a downvote for a specific perk.
     *
     * This method decrements the vote count for the specified perk and
     * redirects back to the dashboard.
     *
     * @param perkId the ID of the perk to downvote
     * @return a redirect to the perks dashboard
     */

    @PostMapping("/{perkId}/downvote")
    public String downvotePerk(@PathVariable Long perkId) {
        perkService.vote(perkId, false);
        return "redirect:/perks/dashboard";
    }






}
