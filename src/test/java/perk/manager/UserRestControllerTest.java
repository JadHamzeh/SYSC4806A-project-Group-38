package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class UserRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMembershipService userMembershipService;

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private UserRestController userRestController;

    private User user;
    private MembershipType membershipType;
    private UserMembership userMembership;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        membershipType = new MembershipType("Scene+");
        membershipType.setId(1L);

        userMembership = new UserMembership(user, membershipType);
    }

    @Test
    void testRegister_Success() {
        Map<String, String> payload = Map.of(
                "username", "newuser",
                "password", "password123"
        );

        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.registerUser("newuser", "password123")).thenReturn(user);

        ResponseEntity<?> response = userRestController.register(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(user.getId(), body.get("id"));
        assertEquals("testuser", body.get("username"));
    }

    @Test
    void testRegister_UsernameExists() {
        Map<String, String> payload = Map.of(
                "username", "existinguser",
                "password", "password123"
        );

        when(userService.findByUsername("existinguser")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userRestController.register(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void testGetUser_Found() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userMembershipService.getMembershipsForUser(user)).thenReturn(List.of(membershipType));

        ResponseEntity<?> response = userRestController.getUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(user.getId(), body.get("id"));
        assertEquals(user.getUsername(), body.get("username"));
        assertEquals(List.of(membershipType), body.get("memberships"));
    }

    @Test
    void testGetUser_NotFound() {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userRestController.getUser(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testAddMembership_Success() {
        Map<String, Long> payload = Map.of("membershipTypeId", 1L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(membershipService.findById(1L)).thenReturn(Optional.of(membershipType));
        when(userMembershipService.assignMembership(user, membershipType)).thenReturn(userMembership);

        ResponseEntity<?> response = userRestController.addMembership(1L, payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userMembership, response.getBody());
    }

    @Test
    void testAddMembership_UserNotFound() {
        Map<String, Long> payload = Map.of("membershipTypeId", 1L);

        when(userService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userRestController.addMembership(999L, payload);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testAddMembership_MembershipNotFound() {
        Map<String, Long> payload = Map.of("membershipTypeId", 999L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(membershipService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userRestController.addMembership(1L, payload);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}