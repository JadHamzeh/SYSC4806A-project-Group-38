package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

class DataLoaderTest {

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private PerkService perkService;

    @InjectMocks
    private DataLoader dataLoader;

    private User demoUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        demoUser = new User();
        demoUser.setId(1L);
        demoUser.setUsername("demo");
    }

    @Test
    void testRun_WhenMembershipTypesEmpty_PreloadsMembershipTypes() {
        when(membershipTypeRepository.count()).thenReturn(0L);
        when(userService.findByUsername("demo")).thenReturn(Optional.of(demoUser));

        dataLoader.run();

        verify(membershipTypeRepository, times(10)).save(any(MembershipType.class));

        ArgumentCaptor<MembershipType> captor = ArgumentCaptor.forClass(MembershipType.class);
        verify(membershipTypeRepository, times(10)).save(captor.capture());

        assertEquals("Air Miles", captor.getAllValues().get(0).getName());
        assertEquals("PC Optimum", captor.getAllValues().get(1).getName());
        assertEquals("Amazon Prime", captor.getAllValues().get(9).getName());
    }

    @Test
    void testRun_WhenMembershipTypesNotEmpty_DoesNotPreload() {
        when(membershipTypeRepository.count()).thenReturn(5L);
        when(userService.findByUsername("demo")).thenReturn(Optional.of(demoUser));

        dataLoader.run();

        verify(membershipTypeRepository, never()).save(any(MembershipType.class));
    }

    @Test
    void testRun_WhenDemoUserNotExists_CreatesDemoUserWithPerks() {
        when(membershipTypeRepository.count()).thenReturn(0L);
        when(userService.findByUsername("demo")).thenReturn(Optional.empty());
        when(userService.registerUser("demo", "demo123")).thenReturn(demoUser);

        dataLoader.run();

        verify(userService, times(1)).registerUser("demo", "demo123");
        verify(perkService, times(4)).createPerk(anyString(), anyString(), anyString(), anyString(), anyLong(), any(LocalDate.class));
    }

    @Test
    void testRun_WhenDemoUserExists_DoesNotCreateUserOrPerks() {
        when(membershipTypeRepository.count()).thenReturn(0L);
        when(userService.findByUsername("demo")).thenReturn(Optional.of(demoUser));

        dataLoader.run();

        verify(userService, never()).registerUser(anyString(), anyString());
        verify(perkService, never()).createPerk(anyString(), anyString(), anyString(), anyString(), anyLong(), any(LocalDate.class));
    }
}