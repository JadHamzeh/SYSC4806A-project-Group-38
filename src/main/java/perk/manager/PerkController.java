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

    private final PerkService perkService;
    private final MembershipTypeRepository membershipTypeRepository;

    @Autowired
    public PerkController(PerkService perkService, MembershipTypeRepository membershipTypeRepository) {
        this.perkService = perkService;
        this.membershipTypeRepository = membershipTypeRepository;
    }

    @GetMapping("/dashboard")
    public String perksPage(@AuthenticationPrincipal User user, Model model) {

        model.addAttribute("isLoggedIn", user != null);
        model.addAttribute("currentUser", user);
        model.addAttribute("perks", perkService.getAllPerks());

        return "dashboard";
    }

    @GetMapping("/dashboard/search")
    public String perkSearch(
            @RequestParam String MembershipType,
            @RequestParam(required = false, defaultValue = "votes") String sortBy,
            Model model) {

        List<Perk> perks;

        if (sortBy.equalsIgnoreCase("relevance")) {
            perks = perkService.searchByMembership(MembershipType);
        } else {
            perks = perkService.getAllPerks();
            if (sortBy.equalsIgnoreCase("votes")) {
                perks.sort(Comparator.comparingInt(Perk::getVotes).reversed());
            } else if (sortBy.equalsIgnoreCase("expiry")) {
                perks.sort(Comparator.comparing(Perk::getExpiryDate));
            }
        }

        model.addAttribute("perks", perks);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("membershipType", MembershipType);

        return "dashboard";
    }

    @GetMapping("/new")
    public String newPerkForm(Model model) {
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        model.addAttribute("perk", new Perk());
        return "new-perk";
    }

    @PostMapping
    public String createPerk(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String region,
            @RequestParam String membershipName,
            @RequestParam LocalDate expiryDate,
            @AuthenticationPrincipal User user,
            Model model) {

        if (user == null) {
            return "redirect:/login";
        }

        Perk createdPerk = perkService.createPerk(
                title, description, region, membershipName, user.getId(), expiryDate);

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
