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

    @GetMapping("/add")
    public String ListMemberships(@AuthenticationPrincipal User user, Model model){
        List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);

        List<MembershipType> Memberships = membershipTypeRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("usermemberships", userMemberships);
        model.addAttribute("Memberships", Memberships);

        return "";


    }

    @GetMapping("/add")
    public String addNewMembership(@AuthenticationPrincipal User user, Model model){
        List<MembershipType> userMemberships = userMembershipService.getMembershipsForUser(user);
        List<MembershipType> availableMemberships = membershipTypeRepository.findAll().stream().filter(m -> !userMemberships.contains(m)).toList();

        model.addAttribute("availableMemberships", availableMemberships);

        return "";
    }

    @PostMapping("/add")
    public String addMemberships(@RequestParam long Id, @AuthenticationPrincipal User user, Model model){
        MembershipType membershipType = membershipTypeRepository.findById(Id).orElseThrow(() -> new RuntimeException("Membership not part of available memberships"));

        userMembershipService.assignMembership(user, membershipType);

        List<MembershipType> newMembershipList = userMembershipService.getMembershipsForUser(user);

        model.addAttribute("userMemberships", newMembershipList);

        return"";

    }

    @PostMapping("/remove")
    public String removeMemberships(@RequestParam long id, @AuthenticationPrincipal User user, Model model){
        UserMembership userMembership = userMembershipRepository.findById(id).orElseThrow(() -> new RuntimeException("Membership not found"));

        userMembershipService.removeMembership(userMembership);

        List<MembershipType> newMembershipList = userMembershipService.getMembershipsForUser(user);
        model.addAttribute("userMemberships", newMembershipList);

        return "";


    }


}
