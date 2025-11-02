package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/perks")
public class PerkController {
    @Autowired
    private PerkService perkService;

    private MembershipTypeRepository membershipTypeRepository;

    @GetMapping("/search")
    public String perkSearch(@RequestParam String MembershipType, Model model){
        if (MembershipType != null){
        }


        return MembershipType;
    }

    @GetMapping("/new")
    public String newPerkForm(Model model) {
        model.addAttribute("memberships", membershipTypeRepository.findAll());
        return "";
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

        Perk createdPerk = perkService.createPerk(title, description, region, membershipName, user.getId(), expiryDate);

        model.addAttribute("perk", createdPerk);


        return "";
    }

    @PostMapping("/{perkId}/upvote")
    public String upvotePerk(@PathVariable Long perkId) {
        perkService.vote(perkId, true);
        return "";
    }




}
