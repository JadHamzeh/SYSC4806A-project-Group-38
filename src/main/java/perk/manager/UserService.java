package perk.manager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class that handles all user-related operations.
 *
 * This service provides methods for registering new users, verifying passwords,
 * and retrieving users by ID or username.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new instance of the user service.
     *
     * @param userRepository the repository used to access user data
     * @param passwordEncoder the encoder used for hashing user passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user by creating a user record, encoding the password,
     * and saving the user in the database.
     *
     * @param username the chosen username of the new user
     * @param rawPassword the unencrypted password provided by the user
     * @return the created user object
     */
    public User registerUser(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    /**
     * Verifies whether a raw password matches a stored hashed password.
     *
     * @param rawPassword the plain password provided during login
     * @param hashedPassword the encoded password stored in the database
     * @return true if the passwords match, false otherwise
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword){
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id the ID of the user to look up
     * @return an optional containing the user if found
     */
    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    /**
     * Retrieves a user by their username.
     *
     * This method scans all users in the repository and returns the first match.
     * It should be replaced with a dedicated query method for improved efficiency.
     *
     * @param username the username to search for
     * @return an optional containing the matching user if found
     */
    public Optional<User> findByUsername(String username){
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }
}
