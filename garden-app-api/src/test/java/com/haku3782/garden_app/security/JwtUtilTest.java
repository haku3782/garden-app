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
        // 末尾1文字の変更はbase64のパディング位置によって同一バイト列にデコードされ
        // 署名が偶然一致してしまうことがあるため、ペイロード部分の中央付近を変更する
        int payloadStart = token.indexOf('.') + 1;
        int mutateIndex = payloadStart + (token.length() - payloadStart) / 2;
        char original = token.charAt(mutateIndex);
        char replacement = original == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, mutateIndex) + replacement + token.substring(mutateIndex + 1);

        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForGarbageInput() {
        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }
}
