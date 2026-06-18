package com.example.employeesearch.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request. {@code @NotBlank} enforces BR-3 r1/r2 (both fields required) at the
 * controller boundary. The error message matches the desktop wording exactly.
 */
public record LoginRequest(
        @NotBlank(message = "Please enter both username and password.") String username,
        @NotBlank(message = "Please enter both username and password.") String password) {
}
