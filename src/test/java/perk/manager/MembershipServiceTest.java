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

class MembershipServiceTest {

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @InjectMocks
    private MembershipService membershipService;

    private MembershipType membership;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        membership = new MembershipType();
        membership.setId(1L);
        membership.setName("PC optimum");
    }

    @Test
    void testGetAllMemberships() {
        when(membershipTypeRepository.findAll()).thenReturn(List.of(membership));

        List<MembershipType> memberships = membershipService.getAllMemberships();

        assertEquals(1, memberships.size());
        assertEquals("PC optimum", memberships.get(0).getName());
        verify(membershipTypeRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        when(membershipTypeRepository.findById(1L)).thenReturn(Optional.of(membership));

        Optional<MembershipType> result = membershipService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(membership, result.get());
        verify(membershipTypeRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(membershipTypeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<MembershipType> result = membershipService.findById(999L);

        assertFalse(result.isPresent());
        verify(membershipTypeRepository, times(1)).findById(999L);
    }

    @Test
    void testSave() {
        when(membershipTypeRepository.save(any(MembershipType.class))).thenReturn(membership);

        MembershipType saved = membershipService.save("PC optimum");

        assertNotNull(saved);
        assertEquals("PC optimum", saved.getName());

        ArgumentCaptor<MembershipType> captor = ArgumentCaptor.forClass(MembershipType.class);
        verify(membershipTypeRepository).save(captor.capture());
        assertEquals("PC optimum", captor.getValue().getName());
    }
}
