package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserMembershipServiceTest {

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserMembershipService userMembershipService;

    private User user;
    private MembershipType membership;
    private UserMembership userMembership;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("Perk user");

        membership = new MembershipType();
        membership.setId(1L);
        membership.setName("Amex");

        userMembership = new UserMembership(user, membership);
    }

    @Test
    void testAssignMembership() {
        when(userMembershipRepository.save(any(UserMembership.class))).thenReturn(userMembership);

        UserMembership assigned = userMembershipService.assignMembership(user, membership);

        assertNotNull(assigned);
        assertEquals(user, assigned.getUser());
        assertEquals(membership, assigned.getMembershipType());

        ArgumentCaptor<UserMembership> captor = ArgumentCaptor.forClass(UserMembership.class);
        verify(userMembershipRepository).save(captor.capture());
        UserMembership saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(membership, saved.getMembershipType());
    }

    @Test
    void testRemoveMembership() {
        doNothing().when(userMembershipRepository).delete(userMembership);

        userMembershipService.removeMembership(userMembership);

        verify(userMembershipRepository, times(1)).delete(userMembership);
    }

    @Test
    void testGetMembershipsForUser() {
        UserMembership um1 = new UserMembership(user, membership);
        MembershipType membership2 = new MembershipType();
        membership2.setId(2L);
        membership2.setName("Pc optimum");
        UserMembership um2 = new UserMembership(user, membership2);

        user.getMemberships().add(um1);
        user.getMemberships().add(um2);

        List<MembershipType> memberships = userMembershipService.getMembershipsForUser(user);

        assertEquals(2, memberships.size());
        assertTrue(memberships.contains(membership));
        assertTrue(memberships.contains(membership2));
    }

    @Test
    void testRemoveMembershipByUserAndType_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMembershipRepository.findAll()).thenReturn(List.of(userMembership));

        userMembershipService.removeMembershipByUserAndType(1L, 1L);

        ArgumentCaptor<List<UserMembership>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMembershipRepository).deleteAll(captor.capture());
        List<UserMembership> deleted = captor.getValue();
        assertEquals(1, deleted.size());
        assertEquals(userMembership, deleted.get(0));
    }

    @Test
    void testRemoveMembershipByUserAndType_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        userMembershipService.removeMembershipByUserAndType(999L, 1L);

        verify(userMembershipRepository, never()).deleteAll(any());
    }

    @Test
    void testRemoveMembershipByUserAndType_NoMatchingMembership() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMembershipRepository.findAll()).thenReturn(List.of(userMembership));

        // Try to remove a different membership type
        userMembershipService.removeMembershipByUserAndType(1L, 999L);

        ArgumentCaptor<List<UserMembership>> captor = ArgumentCaptor.forClass(List.class);
        verify(userMembershipRepository).deleteAll(captor.capture());
        List<UserMembership> deleted = captor.getValue();
        assertEquals(0, deleted.size()); // Should delete empty list
    }

    @Test
    void testGetMembershipsForUser_NullUser() {
        List<MembershipType> memberships = userMembershipService.getMembershipsForUser(null);

        assertEquals(0, memberships.size());
        verify(userMembershipRepository, never()).findAll();
    }

    @Test
    void testGetMembershipsForUser_NoMemberships() {
        when(userMembershipRepository.findAll()).thenReturn(List.of());

        List<MembershipType> memberships = userMembershipService.getMembershipsForUser(user);

        assertEquals(0, memberships.size());
    }
}
