package com.example.employeesearch.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit coverage of token minting and verification (DESIGN §7.2). Uses a fixed test secret;
 * no Spring context required.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-0123456789-abc";

    @Test
    void issueThenParse_roundTripsSubjectAndRoles() {
        JwtService service = new JwtService(SECRET, 60);

        JwtService.IssuedToken issued = service.issue("admin", List.of("ROLE_EMPLOYEE_VIEWER"));
        JwtService.ParsedToken parsed = service.parse(issued.token());

        assertThat(parsed.username()).isEqualTo("admin");
        assertThat(parsed.roles()).containsExactly("ROLE_EMPLOYEE_VIEWER");
    }

    @Test
    void issue_setsExpiryToConfiguredMinutesFromNow() {
        JwtService service = new JwtService(SECRET, 60);

        Instant before = Instant.now();
        JwtService.IssuedToken issued = service.issue("admin", List.of());

        // JWT second-precision truncation can shave up to a second off; allow a small window.
        assertThat(issued.expiresAt())
                .isBetween(before.plus(59, ChronoUnit.MINUTES), before.plus(61, ChronoUnit.MINUTES));
    }

    @Test
    void parse_emptyRolesClaim_yieldsEmptyList() {
        JwtService service = new JwtService(SECRET, 60);

        JwtService.IssuedToken issued = service.issue("admin", List.of());

        assertThat(service.parse(issued.token()).roles()).isEmpty();
    }

    @Test
    void parse_tokenSignedWithDifferentSecret_isRejected() {
        JwtService issuer = new JwtService(SECRET, 60);
        JwtService verifier = new JwtService("a-totally-different-secret-key-0123456789-xyz", 60);

        String token = issuer.issue("admin", List.of()).token();

        assertThatThrownBy(() -> verifier.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void parse_expiredToken_isRejected() {
        // Negative expiry mints a token that is already past its expiration.
        JwtService service = new JwtService(SECRET, -1);

        String token = service.issue("admin", List.of()).token();

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void parse_garbageToken_isRejected() {
        JwtService service = new JwtService(SECRET, 60);

        assertThatThrownBy(() -> service.parse("not-a-jwt")).isInstanceOf(JwtException.class);
    }
}
