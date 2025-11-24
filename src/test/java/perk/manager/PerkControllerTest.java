package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpSession;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerkControllerTest {

    @Mock
    private PerkService perkService;

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private PerkRepository perkRepository;

    @Mock
    private Model model;

    @InjectMocks
    private PerkController perkController;

    private User user;
    private MembershipType membershipType;
    private Perk perk;
    private List<MembershipType> memberships;
    private org.springframework.security.core.userdetails.User principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        membershipType = new MembershipType();
        membershipType.setId(1L);
        membershipType.setName("Aeroplan");

        perk = new Perk("Test Perk", "Description", "Canada",
                LocalDate.now().plusDays(30), membershipType, user);
        perk.setId(1L);
        perk.setVotes(5);

        memberships = Arrays.asList(membershipType);

        principal = (org.springframework.security.core.userdetails.User) org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("hashedpassword")
                .roles("USER")
                .build();
    }

    @Test
    void testPerksPage_WithAuthenticatedUser() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(principal, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("isLoggedIn", true);
        verify(model).addAttribute("currentUser", user);
        verify(model).addAttribute("perks", Arrays.asList(perk));
        verify(model).addAttribute("memberships", memberships);
    }

    @Test
    void testPerksPage_WithoutAuthentication() {
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(null, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("isLoggedIn", false);
        verify(model, never()).addAttribute(eq("currentUser"), any());
    }

    @Test
    void testPerkSearchFragment_AllPerks_SortByVotes() {
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));

        String viewName = perkController.perkSearchFragment(null, "votes", null, model);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(perkService).getAllPerks();
        verify(model).addAttribute(eq("perks"), any());
        verify(model).addAttribute("sortBy", "votes");
    }

    @Test
    void testPerkSearchFragment_FilterByMembership() {
        when(perkService.searchByMembership("Aeroplan")).thenReturn(Arrays.asList(perk));

        String viewName = perkController.perkSearchFragment("Aeroplan", "votes", null, model);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(perkService).searchByMembership("Aeroplan");
        verify(model).addAttribute("selectedMembership", "Aeroplan");
    }

    @Test
    void testPerkSearchFragment_SortByExpiry() {
        Perk perk2 = new Perk("Another Perk", "Desc", "Canada",
                LocalDate.now().plusDays(10), membershipType, user);
        perk2.setId(2L);

        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk, perk2));

        String viewName = perkController.perkSearchFragment(null, "expiry", null, model);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(model).addAttribute("sortBy", "expiry");
    }

    @Test
    void testPerkSearch_FullPage() {
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perkSearch(null, "votes", null, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute(eq("perks"), any());
        verify(model).addAttribute("memberships", memberships);
    }

    @Test
    void testNewPerkForm_Authenticated() {
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.newPerkForm(principal, model);

        assertEquals("fragments/new-perk-form :: new-perk-form", viewName);
        verify(model).addAttribute("memberships", memberships);
    }

    @Test
    void testNewPerkForm_NotAuthenticated() {
        String viewName = perkController.newPerkForm(null, model);

        assertEquals("redirect:/login", viewName);
        verify(membershipTypeRepository, never()).findAll();
    }

    @Test
    void testCreatePerk_Success() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.createPerk(any(), any(), any(), any(), any(), any())).thenReturn(perk);

        String viewName = perkController.createPerk(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), principal, model
        );

        assertEquals("redirect:/perks/dashboard", viewName);
        verify(perkService).createPerk(any(), any(), any(), any(), eq(1L), any());
    }

    @Test
    void testCreatePerk_NotAuthenticated() {
        String viewName = perkController.createPerk(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), null, model
        );

        assertEquals("redirect:/login", viewName);
        verify(perkService, never()).createPerk(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCreatePerk_UserNotFound() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

        String viewName = perkController.createPerk(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), principal, model
        );

        assertEquals("redirect:/login", viewName);
        verify(perkService, never()).createPerk(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCreatePerk_Exception() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.createPerk(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Failed to create"));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.createPerk(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), principal, model
        );

        assertEquals("fragments/new-perk-form :: new-perk-form", viewName);
        verify(model).addAttribute(eq("error"), contains("Failed to create"));
    }

    @Test
    void testCreatePerkFragment_Success() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.createPerk(any(), any(), any(), any(), any(), any())).thenReturn(perk);

        String viewName = perkController.createPerkFragment(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), principal, model
        );

        assertEquals("fragments/new-perk-form :: success-message", viewName);
    }

    @Test
    void testCreatePerkFragment_NotAuthenticated() {
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.createPerkFragment(
                "Test Perk", "Description", "Canada", "Aeroplan",
                LocalDate.now().plusDays(30), null, model
        );

        assertEquals("fragments/new-perk-form :: new-perk-form", viewName);
        verify(model).addAttribute("error", "Please log in to create perks");
    }

    @Test
    void testUpvotePerkFragment() {
        MockHttpSession session = new MockHttpSession();
        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));
        doNothing().when(perkService).vote(1L, true);

        String viewName = perkController.upvotePerkFragment(1L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(perkService).vote(1L, true);
        verify(model).addAttribute("perk", perk);
    }

    @Test
    void testDownvotePerkFragment() {
        MockHttpSession session = new MockHttpSession();
        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));
        doNothing().when(perkService).vote(1L, false);

        String viewName = perkController.downvotePerkFragment(1L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(perkService).vote(1L, false);
        verify(model).addAttribute("perk", perk);
    }

    @Test
    void testUpvotePerkFragment_PerkNotFound() {
        MockHttpSession session = new MockHttpSession();
        when(perkRepository.findById(999L)).thenReturn(Optional.empty());

        String viewName = perkController.upvotePerkFragment(999L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(model, never()).addAttribute(eq("perk"), any());
    }

    @Test
    void testUpvotePerk() {
        MockHttpSession session = new MockHttpSession();
        doNothing().when(perkService).vote(1L, true);

        String viewName = perkController.upvotePerk(1L, session);

        assertEquals("redirect:/perks/dashboard", viewName);
        verify(perkService).vote(1L, true);
    }

    @Test
    void testDownvotePerk() {
        MockHttpSession session = new MockHttpSession();
        doNothing().when(perkService).vote(1L, false);

        String viewName = perkController.downvotePerk(1L, session);

        assertEquals("redirect:/perks/dashboard", viewName);
        verify(perkService).vote(1L, false);
    }
}