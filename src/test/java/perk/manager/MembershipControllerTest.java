package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MembershipControllerTest {

    @Mock private UserMembershipService userMembershipService;
    @Mock private MembershipTypeRepository membershipTypeRepository;
    @Mock private UserMembershipRepository userMembershipRepository;
    @Mock private Model model;

    @InjectMocks private MembershipController membershipController;

    private User user;
    private MembershipType airMiles;
    private MembershipType visa;
    private UserMembership userMembership;

    @BeforeEach
    void setUp() {
        // Initialize all @Mock and @InjectMocks fields manually
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        airMiles = new MembershipType();
        airMiles.setId(1L);
        airMiles.setName("Air Miles");

        visa = new MembershipType();
        visa.setId(2L);
        visa.setName("Visa");

        userMembership = new UserMembership(user, airMiles);
    }

    @Test
    void testAddMemberships() {
        List<MembershipType> updatedList = Arrays.asList(airMiles, visa);
        when(membershipTypeRepository.findById(2L)).thenReturn(Optional.of(visa));
        when(userMembershipService.getMembershipsForUser(user)).thenReturn(updatedList);

        String result = membershipController.addMemberships(2L, user, model);

        assertEquals("", result);
        verify(userMembershipService).assignMembership(user, visa);
        verify(model).addAttribute("userMemberships", updatedList);
    }

    @Test
    void testAddMembership_InvalidId() {
        when(membershipTypeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> membershipController.addMemberships(999L, user, model));
    }

    @Test
    void testRemoveMembership_Success() {
        List<MembershipType> updatedList = Arrays.asList(visa);
        when(userMembershipRepository.findById(1L)).thenReturn(Optional.of(userMembership));
        when(userMembershipService.getMembershipsForUser(user)).thenReturn(updatedList);

        String result = membershipController.removeMemberships(1L, user, model);

        assertEquals("", result);
        verify(userMembershipService).removeMembership(userMembership);
        verify(model).addAttribute("userMemberships", updatedList);
    }

    @Test
    void testRemoveMembership_InvalidId() {
        when(userMembershipRepository.findById(90L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> membershipController.removeMemberships(90L, user, model));
    }
}
