package com.example.employeesearch.security;

import com.example.employeesearch.domain.User;
import com.example.employeesearch.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dev stub of the auth seam (C2, DESIGN §7.1). Active under the {@code dev} and default profiles.
 * Verifies credentials against the existing {@code Users} table with BCrypt — compatible with the
 * existing {@code $2a$11$} hashes without re-hashing (SEC-7 noted, out of scope).
 */
@Component
@Profile({"dev", "default"})
public class DbAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DbAuthenticationProvider(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticatedUser authenticate(String username, String rawPassword) {
        // Exact, case-sensitive lookup (A-4, D8). Same failure for unknown user and bad password (BR-3 r7).
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password. Please try again."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password. Please try again.");
        }

        return new AuthenticatedUser(user.getUsername(), List.of(Roles.EMPLOYEE_VIEWER));
    }
}
