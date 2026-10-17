package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.InvalidCredentialsException;
import dev.tenantguard.tenantguard_lab.exception.ResourceNotFoundException;
import dev.tenantguard.tenantguard_lab.exception.UserAlreadyExistsException;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(username, encodedPassword);

        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
