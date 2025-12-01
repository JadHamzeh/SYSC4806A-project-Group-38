package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;

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
    private MockHttpSession session;

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

        session = new MockHttpSession();
    }

    @Test
    void testPerksPage_WithAuthenticatedUser() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(principal, model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("isLoggedIn", true);
        verify(model).addAttribute("currentUser", user);
        verify(model).addAttribute("perks", Arrays.asList(perk));
        verify(model).addAttribute("memberships", memberships);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerksPage_WithoutAuthentication() {
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(null, model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("isLoggedIn", false);
        verify(model, never()).addAttribute(eq("currentUser"), any());
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerkSearchFragment_AllPerks_SortByVotes() {
        when(perkService.searchPerks(null, null)).thenReturn(Arrays.asList(perk));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearchFragment(null, null, "votes", principal, model, session);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(perkService).searchPerks(null, null);
        verify(model).addAttribute(eq("perks"), any());
        verify(model).addAttribute("sortBy", "votes");
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerkSearchFragment_FilterByMembership() {
        when(perkService.searchPerks("Aeroplan", null)).thenReturn(Arrays.asList(perk));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearchFragment("Aeroplan", null, "votes", principal, model, session);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(perkService).searchPerks("Aeroplan", null);
        verify(model).addAttribute("selectedMembership", "Aeroplan");
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerkSearchFragment_SortByExpiry() {
        Perk perk2 = new Perk("Another Perk", "Desc", "Canada",
                LocalDate.now().plusDays(10), membershipType, user);
        perk2.setId(2L);

        when(perkService.searchPerks(null, null)).thenReturn(Arrays.asList(perk, perk2));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearchFragment(null, null, "expiry", principal, model, session);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(model).addAttribute("sortBy", "expiry");
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerkSearch_FullPage() {
        when(perkService.searchPerks(null, null)).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearch(null, null, "votes", principal, model, session);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute(eq("perks"), any());
        verify(model).addAttribute("memberships", memberships);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
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
        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));
        doNothing().when(perkService).vote(1L, true);

        String viewName = perkController.upvotePerkFragment(1L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(perkService).vote(1L, true);
        verify(model).addAttribute("perk", perk);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> votedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(votedPerks);
        assertTrue(votedPerks.containsKey(1L));
        assertTrue(votedPerks.get(1L));
    }

    @Test
    void testDownvotePerkFragment() {
        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));
        doNothing().when(perkService).vote(1L, false);

        String viewName = perkController.downvotePerkFragment(1L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(perkService).vote(1L, false);
        verify(model).addAttribute("perk", perk);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> votedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(votedPerks);
        assertTrue(votedPerks.containsKey(1L));
        assertFalse(votedPerks.get(1L));
    }

    @Test
    void testUpvotePerkFragment_PerkNotFound() {
        when(perkRepository.findById(999L)).thenReturn(Optional.empty());

        String viewName = perkController.upvotePerkFragment(999L, session, model);

        assertEquals("fragments/perk-list :: vote-section", viewName);
        verify(model, never()).addAttribute(eq("perk"), any());
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testUpvotePerk() {
        doNothing().when(perkService).vote(1L, true);

        String viewName = perkController.upvotePerk(1L, session);

        assertEquals("redirect:/perks/dashboard", viewName);
        verify(perkService).vote(1L, true);

        Map<Long, Boolean> votedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(votedPerks);
        assertTrue(votedPerks.containsKey(1L));
        assertTrue(votedPerks.get(1L));
    }

    @Test
    void testDownvotePerk() {
        doNothing().when(perkService).vote(1L, false);

        String viewName = perkController.downvotePerk(1L, session);

        assertEquals("redirect:/perks/dashboard", viewName);
        verify(perkService).vote(1L, false);

        Map<Long, Boolean> votedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(votedPerks);
        assertTrue(votedPerks.containsKey(1L));
        assertFalse(votedPerks.get(1L));
    }

    @Test
    void testUpvotePerkFragment_AlreadyUpvoted() {
        HashMap<Long, Boolean> votedPerks = new HashMap<>();
        votedPerks.put(1L, true);
        session.setAttribute("votedPerks", votedPerks);

        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));

        String viewName = perkController.upvotePerkFragment(1L, session, model);

        verify(perkService).vote(1L, false);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> updatedVotedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(updatedVotedPerks);
        assertFalse(updatedVotedPerks.containsKey(1L));
    }

    @Test
    void testDownvotePerkFragment_AlreadyDownvoted() {
        HashMap<Long, Boolean> votedPerks = new HashMap<>();
        votedPerks.put(1L, false);
        session.setAttribute("votedPerks", votedPerks);

        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));

        String viewName = perkController.downvotePerkFragment(1L, session, model);

        verify(perkService).vote(1L, true);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> updatedVotedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(updatedVotedPerks);
        assertFalse(updatedVotedPerks.containsKey(1L));
    }

    @Test
    void testPerkSearchFragment_NoResults() {
        when(perkService.searchPerks("NonExistent", null)).thenReturn(Collections.emptyList());
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearchFragment("NonExistent", null, "votes", principal, model, session);

        assertEquals("fragments/perk-list :: perk-list", viewName);
        verify(model).addAttribute("perks", Collections.emptyList());
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testPerkSearchFragment_FilterByMembershipAndKeyword() {
        when(perkService.searchPerks("Aeroplan", "travel")).thenReturn(Arrays.asList(perk));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = perkController.perkSearchFragment("Aeroplan", "travel", "votes", principal, model, session);

        verify(perkService).searchPerks("Aeroplan", "travel");
        verify(model).addAttribute("selectedMembership", "Aeroplan");
        verify(model).addAttribute("keyword", "travel");
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));
    }

    @Test
    void testVotedPerks_InitializedInSession() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(principal, model, session);

        assertEquals("dashboard", viewName);

        verify(model).addAttribute(eq("votedPerks"), any(Map.class));


        verify(model).addAttribute("isLoggedIn", true);
        verify(model).addAttribute("currentUser", user);
        verify(model).addAttribute("perks", Arrays.asList(perk));
        verify(model).addAttribute("memberships", memberships);
    }
    @Test
    void testVotedPerks_PersistsAcrossCalls() {
        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));
        ArgumentCaptor<Map<Long, Boolean>> votedPerksCaptor = ArgumentCaptor.forClass(Map.class);

        perkController.upvotePerkFragment(1L, session, model);

        verify(model, atLeastOnce()).addAttribute(eq("votedPerks"), votedPerksCaptor.capture());

        reset(model);

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(perkService.getAllPerks()).thenReturn(Arrays.asList(perk));
        when(membershipTypeRepository.findAll()).thenReturn(memberships);

        String viewName = perkController.perksPage(principal, model, session);

        assertEquals("dashboard", viewName);

        verify(model).addAttribute(eq("votedPerks"), argThat((Map<Long, Boolean> map) ->
                map.containsKey(1L) && map.get(1L) == true));
    }

    @Test
    void testUpvotePerkFragment_SwitchFromDownvote() {
        HashMap<Long, Boolean> votedPerks = new HashMap<>();
        votedPerks.put(1L, false);
        session.setAttribute("votedPerks", votedPerks);

        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));

        String viewName = perkController.upvotePerkFragment(1L, session, model);

        verify(perkService, times(2)).vote(1L, true);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> updatedVotedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(updatedVotedPerks);
        assertTrue(updatedVotedPerks.containsKey(1L));
        assertTrue(updatedVotedPerks.get(1L));
    }

    @Test
    void testDownvotePerkFragment_SwitchFromUpvote() {
        HashMap<Long, Boolean> votedPerks = new HashMap<>();
        votedPerks.put(1L, true);
        session.setAttribute("votedPerks", votedPerks);

        when(perkRepository.findById(1L)).thenReturn(Optional.of(perk));

        String viewName = perkController.downvotePerkFragment(1L, session, model);

        verify(perkService, times(2)).vote(1L, false);
        verify(model).addAttribute(eq("votedPerks"), any(Map.class));

        Map<Long, Boolean> updatedVotedPerks = (Map<Long, Boolean>) session.getAttribute("votedPerks");
        assertNotNull(updatedVotedPerks);
        assertTrue(updatedVotedPerks.containsKey(1L));
        assertFalse(updatedVotedPerks.get(1L));
    }
}