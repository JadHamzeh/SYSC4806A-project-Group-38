package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes API endpoints for managing Perk objects.
 *
 * This controller allows clients to retrieve perks, search perks by membership
 * type, create new perks, vote on perks, and delete existing perks. It acts as
 * the public API layer on top of the application's service layer.
 */
@RestController
@RequestMapping("/api/perks")
public class PerkRestController {

    @Autowired
    private PerkService perkService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all perks available in the system
     *
     * @return ResponseEntity containing the list of all perks
     */
    @GetMapping
    public ResponseEntity<List<Perk>> getAllPerks() {
        return ResponseEntity.ok(perkService.getAllPerks());
    }

    /**
     * Searches for perks matching the given membership type.
     *
     * @param membershipType the membership category to filter perks by
     * @return ResponseEntity containing the filtered list of perks
     */
    @GetMapping("/search")
    public ResponseEntity<List<Perk>> searchPerks(@RequestParam String membershipType) {
        List<Perk> perks = perkService.searchByMembership(membershipType);
        return ResponseEntity.ok(perks);
    }

    /**
     * Creates a new perk using the provided JSON payload.
     *
     * The payload must contain:
     * title – the name of the perk
     * description – detailed explanation of the perk
     * region – the region where the perk applies
     * membershipType – the associated membership level
     * userId – ID of the user creating the perk
     * expiryDate – ISO-formatted date representing when the perk expires
     *
     * @param payload a map of perk fields posted by the client
     * @return {@link ResponseEntity} containing the created perk or an error message
     */
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

    /**
     * Submits an upvote or downvote for a given perk.
     *
     * @param perkId the ID of the perk to vote on
     * @param upvote true for an upvote, false for downvote
     * @return ResponseEntity indicating success or error
     */
    @PostMapping("/{perkId}/vote")
    public ResponseEntity<?> votePerk(@PathVariable Long perkId, @RequestParam boolean upvote) {
        try {
            perkService.vote(perkId, upvote);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deletes an existing perk by its ID.
     *
     * @param perkId the ID of the perk to delete
     * @return ResponseEntity indicating whether the deletion was successful
     */
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