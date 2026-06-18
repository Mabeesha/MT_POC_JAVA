package com.example.employeesearch.security;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Auth seam (C2, DESIGN §7.1). {@code AuthService} depends only on this interface;
 * the concrete provider (DB dev stub now, AD later) is selected by Spring profile.
 */
public interface AuthenticationProvider {

    /**
     * @return the authenticated user with granted roles.
     * @throws BadCredentialsException when the user is unknown or the password does not match
     *         (same exception for both — prevents username enumeration, BR-3 r7).
     */
    AuthenticatedUser authenticate(String username, String rawPassword);
}
