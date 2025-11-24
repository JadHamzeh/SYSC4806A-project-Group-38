package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/perks")
public class PerkRestController {

    @Autowired
    private PerkService perkService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Perk>> getAllPerks() {
        return ResponseEntity.ok(perkService.getAllPerks());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Perk>> searchPerks(@RequestParam String membershipType) {
        List<Perk> perks = perkService.searchByMembership(membershipType);
        return ResponseEntity.ok(perks);
    }

    @PostMapping
    public ResponseEntity<?> createPerk(@RequestBody Map<String, Object> payload) {
        try {
            String title = (String) payload.get("title");
            String description = (String) payload.get("description");
            String region = (String) payload.get("region");
            String membershipType = (String) payload.get("membershipType");
            Long userId = Long.valueOf(payload.get("userId").toString());
            LocalDate expiryDate = LocalDate.parse((String) payload.get("expiryDate"));

            Perk perk = perkService.createPerk(title, description, region, membershipType, userId, expiryDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(perk);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{perkId}/vote")
    public ResponseEntity<?> votePerk(@PathVariable Long perkId, @RequestParam boolean upvote) {
        try {
            perkService.vote(perkId, upvote);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{perkId}")
    public ResponseEntity<?> deletePerk(@PathVariable Long perkId) {
        try {
            perkService.deletePerk(perkId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}