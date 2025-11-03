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

//    @GetMapping("/dashboard")
//    public String perksPage( @AuthenticationPrincipal User user, Model model) {
//
//        if (user != null) {
//            // User logged in
//            model.addAttribute("isLoggedIn", true);
//            model.addAttribute("currentUser", user);
//        } else {
//            // User NOT logged in
//            model.addAttribute("isLoggedIn", false);
//        }
//
//        model.addAttribute("perks", perkService.getAllPerks());
//        return "dashboard";
//    }

    @GetMapping("/search")
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

    @GetMapping("/new")
    public String newPerkForm(Model model) {
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "new-perk";
    }

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
