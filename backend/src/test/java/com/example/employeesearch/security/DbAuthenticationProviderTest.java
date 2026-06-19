package com.example.employeesearch.security;

import com.example.employeesearch.domain.User;
import com.example.employeesearch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit coverage of the dev DB auth provider (C2, DESIGN §7.1). Unknown user and bad password
 * yield the same generic message (BR-3 r7); success grants the employee-viewer role.
 */
@ExtendWith(MockitoExtension.class)
class DbAuthenticationProviderTest {

    private static final String GENERIC_MESSAGE = "Invalid username or password. Please try again.";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DbAuthenticationProvider provider;

    @Test
    void unknownUser_throwsGenericBadCredentials() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.authenticate("ghost", "whatever"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(GENERIC_MESSAGE);
    }

    @Test
    void wrongPassword_throwsGenericBadCredentials() {
        User user = mockUser("admin", "$2a$11$storedhash");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$11$storedhash")).thenReturn(false);

        assertThatThrownBy(() -> provider.authenticate("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage(GENERIC_MESSAGE);
    }

    @Test
    void correctPassword_returnsUserWithEmployeeViewerRole() {
        User user = mockUser("admin", "$2a$11$storedhash");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "$2a$11$storedhash")).thenReturn(true);

        AuthenticatedUser result = provider.authenticate("admin", "admin123");

        assertThat(result.username()).isEqualTo("admin");
        assertThat(result.roles()).containsExactly(Roles.EMPLOYEE_VIEWER);
    }

    private static User mockUser(String username, String passwordHash) {
        User user = org.mockito.Mockito.mock(User.class);
        lenient().when(user.getUsername()).thenReturn(username);
        lenient().when(user.getPasswordHash()).thenReturn(passwordHash);
        return user;
    }
}
