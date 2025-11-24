package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PerkServiceTest {

    @Mock
    private PerkRepository perkRepository;

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PerkService perkService;

    private MembershipType membershipType;
    private User user;
    private Perk perk;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        membershipType = new MembershipType();
        membershipType.setId(1L);
        membershipType.setName("Gold");

        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");

        perk = new Perk("Title", "Description", "Region", LocalDate.now().plusDays(10), membershipType, user);
        perk.setId(100L);
        perk.setVotes(0);
    }

    @Test
    void testGetAllPerks() {
        when(perkRepository.findAll()).thenReturn(List.of(perk));

        List<Perk> result = perkService.getAllPerks();

        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).getTitle());
        verify(perkRepository, times(1)).findAll();
    }

    @Test
    void testSearchByMembership() {
        when(perkRepository.findByMembershipType_NameIgnoreCase("Gold")).thenReturn(List.of(perk));

        List<Perk> result = perkService.searchByMembership("Gold");

        assertEquals(1, result.size());
        assertEquals("Gold", result.get(0).getMembershipType().getName());
        verify(perkRepository, times(1)).findByMembershipType_NameIgnoreCase("Gold");
    }

    @Test
    void testCreatePerk_Success() {
        when(membershipTypeRepository.findAll()).thenReturn(List.of(membershipType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(perkRepository.save(any(Perk.class))).thenReturn(perk);

        LocalDate expiryDate = LocalDate.now().plusDays(30);
        Perk created = perkService.createPerk("Title", "Desc", "Region", "Gold", 1L, expiryDate);

        assertNotNull(created);
        assertEquals("Title", created.getTitle());
        assertEquals("Gold", created.getMembershipType().getName());

        verify(membershipTypeRepository, times(1)).findAll();
        verify(userRepository, times(1)).findById(1L);
        verify(perkRepository, times(1)).save(any(Perk.class));
    }

    @Test
    void testCreatePerk_MembershipTypeNotFound() {
        when(membershipTypeRepository.findAll()).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                perkService.createPerk("Title", "Desc", "Region", "Gold", 1L, LocalDate.now())
        );

        assertEquals("Membership Type not found", exception.getMessage());
        verify(perkRepository, never()).save(any());
    }

    @Test
    void testCreatePerk_UserNotFound() {
        when(membershipTypeRepository.findAll()).thenReturn(List.of(membershipType));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                perkService.createPerk("Title", "Desc", "Region", "Gold", 1L, LocalDate.now())
        );

        assertEquals("User not found", exception.getMessage());
        verify(perkRepository, never()).save(any());
    }

    @Test
    void testVote_Upvote() {
        when(perkRepository.findById(100L)).thenReturn(Optional.of(perk));
        when(perkRepository.save(any(Perk.class))).thenReturn(perk);

        perkService.vote(100L, true);

        ArgumentCaptor<Perk> captor = ArgumentCaptor.forClass(Perk.class);
        verify(perkRepository).save(captor.capture());

        Perk savedPerk = captor.getValue();
        assertEquals(1, savedPerk.getVotes());
    }

    @Test
    void testVote_Downvote() {
        perk.setVotes(2);
        when(perkRepository.findById(100L)).thenReturn(Optional.of(perk));
        when(perkRepository.save(any(Perk.class))).thenReturn(perk);

        perkService.vote(100L, false);

        ArgumentCaptor<Perk> captor = ArgumentCaptor.forClass(Perk.class);
        verify(perkRepository).save(captor.capture());

        Perk savedPerk = captor.getValue();
        assertEquals(1, savedPerk.getVotes());
    }

    @Test
    void testVote_PerkNotFound() {
        when(perkRepository.findById(999L)).thenReturn(Optional.empty());

        perkService.vote(999L, true);

        verify(perkRepository, never()).save(any());
    }
}
