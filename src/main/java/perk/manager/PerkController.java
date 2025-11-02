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

    @GetMapping("/search")
    public String perkSearch(@RequestParam(required = false) String query, @AuthenticationPrincipal User user, Model model){

        return query;
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

        model.addAttribute("perk", createdPerk);

        return title;
    }

    @PostMapping("/{perkId}/upvote")
    public String upvotePerk(
            @PathVariable Long perkId,
            @AuthenticationPrincipal User user,
            Model model) {

        //Perk perk = perkService.vote(perkId, true);
        //model.addAttribute("perk", perk);


        return "";
    }




}
