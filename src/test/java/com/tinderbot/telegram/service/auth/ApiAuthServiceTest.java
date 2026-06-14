package com.tinderbot.telegram.service.auth;

import com.tinderbot.telegram.entity.UserCredentialsEntity;
import com.tinderbot.telegram.entity.UserEntity;
import com.tinderbot.telegram.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiAuthServiceTest {

    private static final String PASSWORD_HASH = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ApiAuthService apiAuthService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(
                JsonMapper.builder().build(),
                "demo-only-jwt-secret-change-me-in-real-projects-32-plus-chars",
                60);
        apiAuthService = new ApiAuthService(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void login_validCredentials_returnsToken() {
        UserEntity user = demoUser();
        when(userRepository.findByUsernameWithCredentials("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", PASSWORD_HASH)).thenReturn(true);

        assertThat(apiAuthService.login("demo", "password"))
                .isPresent()
                .get()
                .satisfies(response -> {
                    assertThat(response.accessToken()).isNotBlank();
                    assertThat(response.tokenType()).isEqualTo("Bearer");
                });
    }

    @Test
    void login_invalidCredentials_returnsEmpty() {
        UserEntity user = demoUser();
        when(userRepository.findByUsernameWithCredentials("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", PASSWORD_HASH)).thenReturn(false);
        when(userRepository.findByUsernameWithCredentials("unknown")).thenReturn(Optional.empty());

        assertThat(apiAuthService.login("demo", "wrong")).isEmpty();
        assertThat(apiAuthService.login("unknown", "password")).isEmpty();
    }

    private static UserEntity demoUser() {
        UserEntity user = new UserEntity();
        user.setUsername("demo");

        UserCredentialsEntity credentials = new UserCredentialsEntity();
        credentials.setUser(user);
        credentials.setPasswordHash(PASSWORD_HASH);
        user.setCredentials(credentials);
        return user;
    }
}
