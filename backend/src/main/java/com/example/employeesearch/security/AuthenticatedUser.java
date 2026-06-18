package com.example.employeesearch.security;

import java.util.List;

/** Result of a successful authentication through the seam (DESIGN §7.1). */
public record AuthenticatedUser(String username, List<String> roles) {
}
