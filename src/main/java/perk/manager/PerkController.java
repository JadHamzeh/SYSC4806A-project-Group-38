package perk.manager;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/perks")
public class PerkController {

    @Autowired
    private PerkService perkService;

    @Autowired
    private MembershipTypeRepository membershipTypeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PerkRepository perkRepository;

    /**
     * Displays the main dashboard page with all perks.
     * Shows user authentication status and loads all available perks and membership types.
     *
     * @param principal the authenticated user principal, null if user is not logged in
     * @param model     the Spring MVC model to pass data to the view
     * @param session   the HTTP session for storing vote state
     * @return the name of the dashboard view template
     */
    @GetMapping("/dashboard")
    public String perksPage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                            Model model,
                            HttpSession session) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentUser", user);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) session.getAttribute("votedPerks");
        if (votedPerks == null) {
            votedPerks = new HashMap<>();
        }
        model.addAttribute("votedPerks", votedPerks);

        model.addAttribute("perks", perkService.getAllPerks());
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "dashboard";
    }

    /**
     * Returns an HTMX fragment containing filtered and sorted perks.
     * Used for dynamic updates without full page reloads.
     *
     * @param membershipType optional filter by membership type name
     * @param sortBy         sorting criteria: "votes" (default), "expiry", or "relevance"
     * @param principal      the authenticated user principal, null if user is not logged in
     * @param model          the Spring MVC model to pass data to the view
     * @param session        the HTTP session for storing vote state
     * @return the Thymeleaf fragment path for the perk list
     */
    @GetMapping("/search-fragment")
    public String perkSearchFragment(
            @RequestParam(required = false) String membershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model,
            HttpSession session) {

        List<Perk> perks;

        if (membershipType != null && !membershipType.isEmpty()) {
            perks = perkService.searchByMembership(membershipType);
        } else {
            perks = perkService.getAllPerks();
        }

        if (sortBy.equalsIgnoreCase("votes")) {
            perks.sort(Comparator.comparingInt(Perk::getVotes).reversed());
        } else if (sortBy.equalsIgnoreCase("expiry")) {
            perks.sort(Comparator.comparing(Perk::getExpiryDate));
        }

        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentUser", user);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) session.getAttribute("votedPerks");
        if (votedPerks == null) {
            votedPerks = new HashMap<>();
        }
        model.addAttribute("votedPerks", votedPerks);

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("selectedMembership", membershipType);

        return "fragments/perk-list :: perk-list";
    }

    /**
     * Performs a full page search and filtering of perks.
     * Returns the complete dashboard with filtered and sorted results.
     *
     * @param membershipType optional filter by membership type name
     * @param sortBy         sorting criteria: "votes" (default), "expiry", or "relevance"
     * @param principal      the authenticated user principal, null if user is not logged in
     * @param model          the Spring MVC model to pass data to the view
     * @param session        the HTTP session for storing vote state
     * @return the name of the dashboard view template with filtered results
     */
    @GetMapping("/search")
    public String perkSearch(
            @RequestParam(required = false) String membershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model,
            HttpSession session) {

        List<Perk> perks;

        if (membershipType != null && !membershipType.isEmpty()) {
            perks = perkService.searchByMembership(membershipType);
        } else {
            perks = perkService.getAllPerks();
        }

        if (sortBy.equalsIgnoreCase("votes")) {
            perks.sort(Comparator.comparingInt(Perk::getVotes).reversed());
        } else if (sortBy.equalsIgnoreCase("expiry")) {
            perks.sort(Comparator.comparing(Perk::getExpiryDate));
        }

        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentUser", user);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        // Add vote state to model
        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) session.getAttribute("votedPerks");
        if (votedPerks == null) {
            votedPerks = new HashMap<>();
        }
        model.addAttribute("votedPerks", votedPerks);

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        model.addAttribute("selectedMembership", membershipType);

        return "dashboard";
    }

    /**
     * Displays the form for creating a new perk as a Thymeleaf fragment.
     * Requires user authentication - redirects to login if not authenticated.
     *
     * @param principal the authenticated user principal
     * @param model     the Spring MVC model to pass data to the view
     * @return the Thymeleaf fragment path for the new perk form, or redirect to login
     */
    @GetMapping("/new")
    public String newPerkForm(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                              Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "fragments/new-perk-form :: new-perk-form";
    }

    /**
     * Handles the creation of a new perk with full page navigation.
     * Validates user authentication and delegates to the service layer.
     *
     * @param title          the title of the perk
     * @param description    detailed description of the perk
     * @param region         the geographical region where the perk is valid
     * @param membershipType the membership type associated with this perk
     * @param expiryDate     the date when the perk expires
     * @param principal      the authenticated user principal
     * @param model          the Spring MVC model to pass data to the view
     * @return redirect to dashboard on success, or back to form with error
     */
    @PostMapping("/create")
    public String createPerk(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String region,
            @RequestParam String membershipType,
            @RequestParam LocalDate expiryDate,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            perkService.createPerk(title, description, region, membershipType, user.getId(), expiryDate);
            return "redirect:/perks/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create perk: " + e.getMessage());
            model.addAttribute("memberships", membershipTypeRepository.findAll());
            return "fragments/new-perk-form :: new-perk-form";
        }
    }

    /**
     * Handles the creation of a new perk via HTMX fragment submission.
     * Returns appropriate fragment responses for success or error states.
     *
     * @param title          the title of the perk
     * @param description    detailed description of the perk
     * @param region         the geographical region where the perk is valid
     * @param membershipType the membership type associated with this perk
     * @param expiryDate     the date when the perk expires
     * @param principal      the authenticated user principal
     * @param model          the Spring MVC model to pass data to the view
     * @return success message fragment on success
     */
    @PostMapping("/create-fragment")
    public String createPerkFragment(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String region,
            @RequestParam String membershipType,
            @RequestParam LocalDate expiryDate,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model) {

        if (principal == null) {
            model.addAttribute("error", "Please log in to create perks");
            model.addAttribute("memberships", membershipTypeRepository.findAll());
            return "fragments/new-perk-form :: new-perk-form";
        }

        User user = userService.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            model.addAttribute("memberships", membershipTypeRepository.findAll());
            return "fragments/new-perk-form :: new-perk-form";
        }

        try {
            perkService.createPerk(title, description, region, membershipType, user.getId(), expiryDate);
            return "fragments/new-perk-form :: success-message";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create perk: " + e.getMessage());
            model.addAttribute("memberships", membershipTypeRepository.findAll());
            return "fragments/new-perk-form :: new-perk-form";
        }
    }

    /**
     * Handles upvoting a perk via HTMX fragment request.
     * Returns the updated vote section fragment with new vote count.
     *
     * @param perkId the ID of the perk to upvote
     * @param sesh   the HTTP session for storing vote state
     * @param model  the Spring MVC model to pass data to the view
     * @return the Thymeleaf fragment path for the vote section
     */
    @PostMapping("/{perkId}/upvote-fragment")
    public String upvotePerkFragment(@PathVariable Long perkId, HttpSession sesh, Model model) {
        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) sesh.getAttribute("votedPerks");
        if (votedPerks == null){
            votedPerks = new HashMap<>();
        }

        Boolean lastVote = votedPerks.get(perkId);

        if (lastVote == null){
            perkService.vote(perkId, true);
            votedPerks.put(perkId, true);
        } else if (lastVote == true) {
            perkService.vote(perkId, false);
            votedPerks.remove(perkId);
        } else if (lastVote == false) {
            perkService.vote(perkId, true);
            perkService.vote(perkId, true);
            votedPerks.put(perkId, true);
        }

        sesh.setAttribute("votedPerks", votedPerks);

        Optional<Perk> perkOpt = perkRepository.findById(perkId);
        if (perkOpt.isPresent()) {
            model.addAttribute("perk", perkOpt.get());
        }

        model.addAttribute("votedPerks", votedPerks);

        return "fragments/perk-list :: vote-section";
    }

    /**
     * Handles downvoting a perk via HTMX fragment request.
     * Returns the updated vote section fragment with new vote count.
     *
     * @param perkId the ID of the perk to downvote
     * @param sesh   the HTTP session for storing vote state
     * @param model  the Spring MVC model to pass data to the view
     * @return the Thymeleaf fragment path for the vote section
     */
    @PostMapping("/{perkId}/downvote-fragment")
    public String downvotePerkFragment(@PathVariable Long perkId, HttpSession sesh, Model model) {
        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) sesh.getAttribute("votedPerks");
        if (votedPerks == null){
            votedPerks = new HashMap<>();
        }

        Boolean lastVote = votedPerks.get(perkId);

        if (lastVote == null){
            perkService.vote(perkId, false);
            votedPerks.put(perkId, false);
        } else if (lastVote == false) {
            perkService.vote(perkId, true);
            votedPerks.remove(perkId);
        } else if (lastVote == true) {
            perkService.vote(perkId, false);
            perkService.vote(perkId, false);
            votedPerks.put(perkId, false);
        }

        sesh.setAttribute("votedPerks", votedPerks);

        Optional<Perk> perkOpt = perkRepository.findById(perkId);
        if (perkOpt.isPresent()) {
            model.addAttribute("perk", perkOpt.get());
        }

        model.addAttribute("votedPerks", votedPerks);

        return "fragments/perk-list :: vote-section";
    }

    /**
     * Handles upvoting a perk with full page navigation.
     * Increments the vote count and redirects back to the dashboard.
     *
     * @param perkId the ID of the perk to upvote
     * @param sesh   the HTTP session for storing vote state
     * @return redirect to the dashboard page
     */
    @PostMapping("/{perkId}/upvote")
    public String upvotePerk(@PathVariable Long perkId, HttpSession sesh) {
        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) sesh.getAttribute("votedPerks");
        if (votedPerks == null){
            votedPerks = new HashMap<>();
        }

        Boolean lastVote = votedPerks.get(perkId);

        if (lastVote == null){
            perkService.vote(perkId, true);
            votedPerks.put(perkId, true);
        } else if (lastVote == true) {
            perkService.vote(perkId, false);
            votedPerks.remove(perkId);
        } else if (lastVote == false) {
            perkService.vote(perkId, true);
            perkService.vote(perkId, true);
            votedPerks.put(perkId, true);
        }

        sesh.setAttribute("votedPerks", votedPerks);
        return "redirect:/perks/dashboard";
    }

    /**
     * Handles downvoting a perk with full page navigation.
     * Decrements the vote count and redirects back to the dashboard.
     *
     * @param perkId the ID of the perk to downvote
     * @param sesh   the HTTP session for storing vote state
     * @return redirect to the dashboard page
     */
    @PostMapping("/{perkId}/downvote")
    public String downvotePerk(@PathVariable Long perkId, HttpSession sesh) {
        HashMap<Long, Boolean> votedPerks = (HashMap<Long, Boolean>) sesh.getAttribute("votedPerks");
        if (votedPerks == null) {
            votedPerks = new HashMap<>();
        }

        Boolean lastVote = votedPerks.get(perkId);

        if (lastVote == null) {
            perkService.vote(perkId, false);
            votedPerks.put(perkId, false);
        } else if (lastVote == false) {
            perkService.vote(perkId, true);
            votedPerks.remove(perkId);
        } else if (lastVote) {
            perkService.vote(perkId, false);
            perkService.vote(perkId, false);
            votedPerks.put(perkId, false);
        }

        sesh.setAttribute("votedPerks", votedPerks);
        return "redirect:/perks/dashboard";
    }
}