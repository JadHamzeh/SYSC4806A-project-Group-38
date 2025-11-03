package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PerkRestControllerTest {

    @Mock
    private PerkService perkService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PerkRestController perkRestController;

    private Perk perk;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        MembershipType membershipType = new MembershipType("Scene+");

        perk = new Perk();
        perk.setId(1L);
        perk.setTitle("Test Perk");
        perk.setDescription("Test Description");
        perk.setRegion("Canada");
        perk.setExpiryDate(LocalDate.now().plusDays(30));
        perk.setMembershipType(membershipType);
        perk.setCreatedBy(user);
    }

    @Test
    void testGetAllPerks() {
        when(perkService.getAllPerks()).thenReturn(List.of(perk));

        ResponseEntity<List<Perk>> response = perkRestController.getAllPerks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(perk, response.getBody().get(0));
    }

    @Test
    void testSearchPerks() {
        when(perkService.searchByMembership("Scene+")).thenReturn(List.of(perk));

        ResponseEntity<List<Perk>> response = perkRestController.searchPerks("Scene+");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(perkService).searchByMembership("Scene+");
    }

    @Test
    void testCreatePerk_Success() {
        Map<String, Object> payload = Map.of(
                "title", "New Perk",
                "description", "New Description",
                "region", "Canada",
                "membershipType", "Scene+",
                "userId", "1",
                "expiryDate", LocalDate.now().plusDays(30).toString()
        );

        when(perkService.createPerk(anyString(), anyString(), anyString(), anyString(), anyLong(), any(LocalDate.class)))
                .thenReturn(perk);

        ResponseEntity<?> response = perkRestController.createPerk(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(perk, response.getBody());
    }

    @Test
    void testCreatePerk_InvalidPayload() {
        Map<String, Object> payload = Map.of("title", "New Perk");

        ResponseEntity<?> response = perkRestController.createPerk(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void testVotePerk_Success() {
        ResponseEntity<?> response = perkRestController.votePerk(1L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(perkService).vote(1L, true);
    }

    @Test
    void testVotePerk_Failure() {
        doThrow(new RuntimeException("Perk not found")).when(perkService).vote(1L, true);

        ResponseEntity<?> response = perkRestController.votePerk(1L, true);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void testDeletePerk_Success() {
        ResponseEntity<?> response = perkRestController.deletePerk(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(perkService).deletePerk(1L);
    }

    @Test
    void testDeletePerk_Failure() {
        doThrow(new RuntimeException("Perk not found")).when(perkService).deletePerk(1L);

        ResponseEntity<?> response = perkRestController.deletePerk(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }
}