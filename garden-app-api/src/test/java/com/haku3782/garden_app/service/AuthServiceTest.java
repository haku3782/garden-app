package com.haku3782.garden_app.service;

import com.haku3782.garden_app.domain.User;
import com.haku3782.garden_app.dto.AuthRequest;
import com.haku3782.garden_app.dto.AuthResponse;
import com.haku3782.garden_app.repository.UserRepository;
import com.haku3782.garden_app.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesHashedPasswordAndReturnsToken() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("raw-password");

        when(userRepository.findByUsername("taro")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");
        when(jwtUtil.generateToken("taro")).thenReturn("dummy-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("dummy-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsWhenUsernameTooShort() {
        AuthRequest request = new AuthRequest();
        request.setUsername("ab");
        request.setPassword("validpass");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("3〜30文字");
    }

    @Test
    void register_throwsWhenUsernameTooLong() {
        AuthRequest request = new AuthRequest();
        request.setUsername("a".repeat(31));
        request.setPassword("validpass");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("3〜30文字");
    }

    @Test
    void register_throwsWhenPasswordTooShort() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("short");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("8〜72文字");
    }

    @Test
    void register_throwsWhenPasswordTooLong() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("a".repeat(73));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("8〜72文字");
    }

    @Test
    void register_succeedsWithBoundaryValidInput() {
        AuthRequest request = new AuthRequest();
        request.setUsername("abc");
        request.setPassword("12345678");

        when(userRepository.findByUsername("abc")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("12345678")).thenReturn("hashed");
        when(jwtUtil.generateToken("abc")).thenReturn("token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token");
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("raw-password");

        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("すでに使われています");
    }

    @Test
    void login_returnsTokenWhenPasswordMatches() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("raw-password");

        User stored = new User();
        stored.setUsername("taro");
        stored.setPassword("hashed-password");

        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("raw-password", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken("taro")).thenReturn("dummy-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("dummy-token");
    }

    @Test
    void login_throwsWhenUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setUsername("ghost");
        request.setPassword("raw-password");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("見つかりません");
    }

    @Test
    void login_throwsWhenPasswordDoesNotMatch() {
        AuthRequest request = new AuthRequest();
        request.setUsername("taro");
        request.setPassword("wrong-password");

        User stored = new User();
        stored.setUsername("taro");
        stored.setPassword("hashed-password");

        when(userRepository.findByUsername("taro")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("パスワードが違います");
    }
}
