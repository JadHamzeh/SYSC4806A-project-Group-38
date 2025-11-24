package perk.manager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword){
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }
}
