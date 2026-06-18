package com.example.employeesearch.security;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Active Directory provider placeholder (C2, DESIGN §7.1). Active under the {@code prod} profile.
 *
 * <p>TODO (AD): bind to AD/LDAP, verify the credentials, and map AD security groups to app roles
 * (e.g. {@code EmployeeSearch-Users} → {@link Roles#EMPLOYEE_VIEWER}). No LDAP config or secrets
 * are committed here (C2).
 */
@Component
@Profile("prod")
public class AdAuthenticationProvider implements AuthenticationProvider {

    @Override
    public AuthenticatedUser authenticate(String username, String rawPassword) {
        // TODO (AD): bind to AD/LDAP, map group -> role.
        throw new UnsupportedOperationException("AD authentication not yet implemented (TODO (AD)).");
    }
}
