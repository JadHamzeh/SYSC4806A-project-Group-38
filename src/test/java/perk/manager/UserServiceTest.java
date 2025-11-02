package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setPassword("hashed_password");
    }

    @Test
    void testRegisterUser() {
        String rawPassword = "mypassword";
        String hashedPassword = "hashed_password";

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registered = userService.registerUser("john_doe", rawPassword);

        assertNotNull(registered);
        assertEquals("john_doe", registered.getUsername());
        assertEquals("hashed_password", registered.getPassword());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("john_doe", saved.getUsername());
        assertEquals("hashed_password", saved.getPassword());
    }

    @Test
    void testVerifyPassword_Match() {
        when(passwordEncoder.matches("mypassword", "hashed_password")).thenReturn(true);

        boolean result = userService.verifyPassword("mypassword", "hashed_password");
        assertTrue(result);
    }

    @Test
    void testVerifyPassword_NoMatch() {
        when(passwordEncoder.matches("wrongpass", "hashed_password")).thenReturn(false);

        boolean result = userService.verifyPassword("wrongpass", "hashed_password");
        assertFalse(result);
    }

    @Test
    void testFindById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByUsername_Found() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane_doe");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        Optional<User> result = userService.findByUsername("jane_doe");
        assertTrue(result.isPresent());
        assertEquals(user2, result.get());
    }

    @Test
    void testFindByUsername_NotFound() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Optional<User> result = userService.findByUsername("nonexistent");
        assertFalse(result.isPresent());
    }
}
