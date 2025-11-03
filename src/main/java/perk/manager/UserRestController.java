package perk.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMembershipService userMembershipService;

    @Autowired
    private MembershipService membershipService;

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