package com.example.employeesearch.web.dto;

import java.time.Instant;

/** Login response (DESIGN §5.3). Never carries password or hash. */
public record LoginResponse(String token, String username, Instant expiresAt) {
}
