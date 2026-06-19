package com.example.employeesearch.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The prod AD provider is a placeholder and must fail loudly until implemented (C2, TODO (AD)). */
class AdAuthenticationProviderTest {

    @Test
    void authenticate_isNotYetImplemented() {
        AdAuthenticationProvider provider = new AdAuthenticationProvider();

        assertThatThrownBy(() -> provider.authenticate("user", "pass"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
