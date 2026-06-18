package com.example.employeesearch.web;

import com.example.employeesearch.service.AuthService;
import com.example.employeesearch.web.dto.LoginRequest;
import com.example.employeesearch.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Auth endpoints A1/A2 (DESIGN §5.2). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** A1 — Login (FR-2, BR-3). Blank fields → 400 (DTO @NotBlank); bad creds → 401. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    /** A2 — Logout (FR-6). Stateless: client discards the token; server has nothing to invalidate (D1). */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
