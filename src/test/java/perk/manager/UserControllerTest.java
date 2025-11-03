package perk.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    private User user;
    private org.springframework.security.core.userdetails.User principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        principal = new org.springframework.security.core.userdetails.User(
                "testuser", "password", java.util.List.of()
        );
    }

    @Test
    void testHome() {
        String viewName = userController.home();

        assertEquals("index", viewName);
    }

    @Test
    void testLoginPage() {
        String viewName = userController.loginPage();

        assertEquals("login", viewName);
    }

    @Test
    void testSignupPage() {
        String viewName = userController.signupPage();

        assertEquals("signup", viewName);
    }

    @Test
    void testRegister_Success() {
        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.registerUser("newuser", "password")).thenReturn(user);

        String viewName = userController.register("newuser", "password", "email@test.com", model);

        assertEquals("redirect:/login?registered", viewName);
        verify(userService).registerUser("newuser", "password");
    }

    @Test
    void testRegister_UsernameExists() {
        when(userService.findByUsername("existinguser")).thenReturn(Optional.of(user));

        String viewName = userController.register("existinguser", "password", "email@test.com", model);

        assertEquals("signup", viewName);
        verify(model).addAttribute("error", "Username already exists");
        verify(userService, never()).registerUser(anyString(), anyString());
    }

    @Test
    void testRegister_Exception() {
        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.registerUser("newuser", "password")).thenThrow(new RuntimeException("Database error"));

        String viewName = userController.register("newuser", "password", "email@test.com", model);

        assertEquals("signup", viewName);
        verify(model).addAttribute("error", "Registration failed");
    }

    @Test
    void testProfile_WhenUserLoggedIn() {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        String viewName = userController.profile(principal, model);

        assertEquals("profile", viewName);
        verify(model).addAttribute("user", user);
    }

    @Test
    void testProfile_WhenUserNotLoggedIn() {
        String viewName = userController.profile(null, model);

        assertEquals("profile", viewName);
        verify(model, never()).addAttribute(eq("user"), any());
    }
}