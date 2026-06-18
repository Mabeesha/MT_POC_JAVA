package com.example.employeesearch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Issues and verifies signed JWTs (DESIGN §7.2). Secret/expiry injected from config (no secrets in source). */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    /** Mint a token whose subject is the username, carrying the granted roles. */
    public IssuedToken issue(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiryMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .subject(username)
                .claim(ROLES_CLAIM, roles)
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /** Parse and verify a token; throws {@link io.jsonwebtoken.JwtException} when invalid/expired. */
    @SuppressWarnings("unchecked")
    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        return new ParsedToken(claims.getSubject(), roles == null ? List.of() : roles);
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public record ParsedToken(String username, List<String> roles) {
    }
}
