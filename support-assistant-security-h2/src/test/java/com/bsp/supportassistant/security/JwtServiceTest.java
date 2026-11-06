package com.bsp.supportassistant.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Plain JUnit. No Spring, no database.
class JwtServiceTest {

    private static final String SECRET = "a-test-secret-that-is-at-least-32-characters-long";

    private final JwtService jwtService = new JwtService(SECRET, 30);

    @Test
    void aTokenGivesBackTheUserName() {
        String token = jwtService.createToken("meera");

        assertThat(jwtService.readUsername(token)).isEqualTo("meera");
    }

    @Test
    void aChangedTokenIsRefused() {
        String token = jwtService.createToken("meera");
        String changed = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.readUsername(changed)).isInstanceOf(JwtException.class);
    }

    @Test
    void aTokenSignedWithAnotherSecretIsRefused() {
        String token = new JwtService("another-secret-that-is-at-least-32-characters", 30).createToken("meera");

        assertThatThrownBy(() -> jwtService.readUsername(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void anExpiredTokenIsRefused() {
        String token = new JwtService(SECRET, -1).createToken("meera");   // expired one minute ago

        assertThatThrownBy(() -> jwtService.readUsername(token)).isInstanceOf(JwtException.class);
    }
}
