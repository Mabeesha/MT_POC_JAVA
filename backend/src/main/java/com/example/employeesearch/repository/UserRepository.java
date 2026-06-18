package com.example.employeesearch.repository;

import com.example.employeesearch.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Exact, case-sensitive lookup — preserves SQLite BINARY collation behavior (A-4, D8, BR-3).
     */
    Optional<User> findByUsername(String username);
}
