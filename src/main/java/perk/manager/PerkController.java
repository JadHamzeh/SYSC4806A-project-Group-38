package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/dashboard")
    public String perksPage(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                            Model model) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getUsername()).orElse(null);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentUser", user);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        model.addAttribute("perks", perkService.getAllPerks());
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/search-fragment")
    public String perkSearchFragment(
            @RequestParam(required = false) String membershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model) {

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

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("selectedMembership", membershipType);

        return "fragments/perk-list :: perk-list";
    }

    @GetMapping("/search")
    public String perkSearch(
            @RequestParam(required = false) String membershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            Model model) {

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

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        model.addAttribute("selectedMembership", membershipType);

        return "dashboard";
    }

    @GetMapping("/new")
    public String newPerkForm(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                              Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "fragments/new-perk-form :: new-perk-form";
    }

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

    @PostMapping("/{perkId}/upvote-fragment")
    public String upvotePerkFragment(@PathVariable Long perkId, Model model) {
        perkService.vote(perkId, true);
        Optional<Perk> perkOpt = perkRepository.findById(perkId);
        if (perkOpt.isPresent()) {
            model.addAttribute("perk", perkOpt.get());
            return "fragments/perk-list :: vote-section";
        }
        return "fragments/perk-list :: vote-section";
    }

    @PostMapping("/{perkId}/downvote-fragment")
    public String downvotePerkFragment(@PathVariable Long perkId, Model model) {
        perkService.vote(perkId, false);
        Optional<Perk> perkOpt = perkRepository.findById(perkId);
        if (perkOpt.isPresent()) {
            model.addAttribute("perk", perkOpt.get());
            return "fragments/perk-list :: vote-section";
        }
        return "fragments/perk-list :: vote-section";
    }

    @PostMapping("/{perkId}/upvote")
    public String upvotePerk(@PathVariable Long perkId) {
        perkService.vote(perkId, true);
        return "redirect:/perks/dashboard";
    }

    @PostMapping("/{perkId}/downvote")
    public String downvotePerk(@PathVariable Long perkId) {
        perkService.vote(perkId, false);
        return "redirect:/perks/dashboard";
    }
}