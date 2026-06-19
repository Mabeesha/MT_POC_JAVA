package com.example.employeesearch.service;

import com.example.employeesearch.security.AuthenticatedUser;
import com.example.employeesearch.security.AuthenticationProvider;
import com.example.employeesearch.security.JwtService;
import com.example.employeesearch.web.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Login orchestration (DESIGN §5.4, §10.1): username trimming, JWT minting, failure propagation. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_mintsTokenAndReturnsResponse() {
        Instant expiry = Instant.now().plusSeconds(3600);
        when(authenticationProvider.authenticate("admin", "admin123"))
                .thenReturn(new AuthenticatedUser("admin", List.of("ROLE_EMPLOYEE_VIEWER")));
        when(jwtService.issue("admin", List.of("ROLE_EMPLOYEE_VIEWER")))
                .thenReturn(new JwtService.IssuedToken("tok-123", expiry));

        LoginResponse response = authService.login("admin", "admin123");

        assertThat(response.token()).isEqualTo("tok-123");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.expiresAt()).isEqualTo(expiry);
    }

    @Test
    void login_trimsUsernameButNotPassword() {
        when(authenticationProvider.authenticate(any(), any()))
                .thenReturn(new AuthenticatedUser("admin", List.of()));
        when(jwtService.issue(any(), anyList()))
                .thenReturn(new JwtService.IssuedToken("tok", Instant.now()));

        authService.login("  admin  ", "  pw with spaces  ");

        verify(authenticationProvider).authenticate(eq("admin"), eq("  pw with spaces  "));
    }

    @Test
    void login_nullUsername_isTreatedAsEmpty() {
        when(authenticationProvider.authenticate(eq(""), any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(null, "pw"))
                .isInstanceOf(BadCredentialsException.class);

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        verify(authenticationProvider).authenticate(userCaptor.capture(), any());
        assertThat(userCaptor.getValue()).isEmpty();
    }

    @Test
    void login_badCredentials_propagatesAndDoesNotMintToken() {
        when(authenticationProvider.authenticate("admin", "wrong"))
                .thenThrow(new BadCredentialsException("Invalid username or password. Please try again."));

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password. Please try again.");

        verifyNoInteractions(jwtService);
    }
}
