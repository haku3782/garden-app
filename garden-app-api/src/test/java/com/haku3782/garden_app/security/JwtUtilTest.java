package com.haku3782.garden_app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // @Value で注入される secret はテストでは直接セットする（32バイト以上必要）
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-util-unit-test-32bytes");
    }

    @Test
    void generateToken_returnsTokenContainingUsernameAsSubject() {
        String token = jwtUtil.generateToken("taro");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("taro");
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken("taro");

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken("taro");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForGarbageInput() {
        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }
}
