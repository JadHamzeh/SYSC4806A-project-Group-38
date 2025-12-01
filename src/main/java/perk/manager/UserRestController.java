package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller that manages user-related API operations.
 *
 * This controller provides endpoints for user registration, retrieving user details,
 * and assigning membership types to users.
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMembershipService userMembershipService;

    @Autowired
    private MembershipService membershipService;

    /**
     * Registers a new user using the provided username and password.
     *
     * The payload must contain the fields:
     * - username
     * - password
     *
     * If the username already exists, an error response is returned.
     *
     * @param payload map containing user registration details
     * @return a response containing the new user's ID and username, or an error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            String username = payload.get("username");
            String password = payload.get("password");

            if (userService.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }

            User user = userService.registerUser(username, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves a user's information by their ID.
     *
     * The response includes:
     * - user ID
     * - username
     * - memberships assigned to the user
     *
     * If the user does not exist, a not found response is returned.
     *
     * @param userId the ID of the user to retrieve
     * @return a response containing user details or a not found status
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            return ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "memberships", userMembershipService.getMembershipsForUser(u)
            ));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Assigns a membership type to the specified user.
     *
     * The payload must contain:
     * - membershipTypeId
     *
     * If either the user or the membership type does not exist, a not found response is returned.
     *
     * @param userId the ID of the user receiving the membership
     * @param payload map containing the membershipTypeId
     * @return a response containing the created user membership record or an error message
     */
    @PostMapping("/{userId}/memberships")
    public ResponseEntity<?> addMembership(@PathVariable Long userId, @RequestBody Map<String, Long> payload) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            Optional<MembershipType> membershipOpt = membershipService.findById(payload.get("membershipTypeId"));

            if (userOpt.isEmpty() || membershipOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserMembership um = userMembershipService.assignMembership(userOpt.get(), membershipOpt.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(um);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}