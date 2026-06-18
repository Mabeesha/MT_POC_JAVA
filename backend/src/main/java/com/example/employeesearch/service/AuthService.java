package com.example.employeesearch.service;

import com.example.employeesearch.security.AuthenticatedUser;
import com.example.employeesearch.security.AuthenticationProvider;
import com.example.employeesearch.security.JwtService;
import com.example.employeesearch.web.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Login orchestration (DESIGN §5.4, §10.1). Trims the username (not the password, BR-3),
 * delegates to the auth seam, mints a JWT on success, and writes audit log lines.
 */
@Service
public class AuthService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.example.employeesearch.audit");

    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;

    public AuthService(AuthenticationProvider authenticationProvider, JwtService jwtService) {
        this.authenticationProvider = authenticationProvider;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String rawUsername, String password) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        try {
            AuthenticatedUser user = authenticationProvider.authenticate(username, password);
            JwtService.IssuedToken issued = jwtService.issue(user.username(), user.roles());
            AUDIT.info("login success username=\"{}\"", user.username());
            return new LoginResponse(issued.token(), user.username(), issued.expiresAt());
        } catch (BadCredentialsException ex) {
            AUDIT.info("login failure username=\"{}\"", username);
            throw ex;
        }
    }
}
