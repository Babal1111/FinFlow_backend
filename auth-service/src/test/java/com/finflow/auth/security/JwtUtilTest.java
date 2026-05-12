package com.finflow.auth.security;

import com.finflow.auth.entity.Role;
import com.finflow.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;
    private final String SECRET = "mySecretKeyForTestingPurposeWhichIsAtLeast256BitsLong";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 900000L); // 15 min

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRole(Role.APPLICANT);
    }

    @Test
    @DisplayName("JwtUtil: should generate and extract token correctly")
    void tokenLifecycle() {
        String token = jwtUtil.generateAccessToken(user);
        assertNotNull(token);

        assertEquals("test@test.com", jwtUtil.extractEmail(token));
        assertEquals("APPLICANT", jwtUtil.extractRole(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    @DisplayName("JwtUtil: should return false for invalid token")
    void invalidToken() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }
}
