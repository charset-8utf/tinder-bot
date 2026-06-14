package com.tinderbot.telegram.service.auth;

import com.tinderbot.telegram.entity.UserCredentialsEntity;
import com.tinderbot.telegram.entity.UserEntity;
import com.tinderbot.telegram.repository.UserCredentialsRepository;
import com.tinderbot.telegram.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramUserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialsRepository credentialsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private TelegramUserRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new TelegramUserRegistrationService(userRepository, credentialsRepository, passwordEncoder);
    }

    @Test
    void ensureRegistered_whenUserExists_returnsExistingWithoutPassword() {
        UserEntity existing = new UserEntity();
        existing.setUsername("igor");
        when(userRepository.findByTelegramUserId(1767376980L)).thenReturn(Optional.of(existing));

        TelegramUserRegistrationService.RegistrationResult result =
                service.ensureRegistered(1767376980L, Optional.of("igor_dev"));

        assertThat(result.newlyRegistered()).isFalse();
        assertThat(result.username()).isEqualTo("igor");
        assertThat(result.oneTimeValue()).isNull();
        verify(userRepository, never()).save(any());
        verify(credentialsRepository, never()).save(any());
    }

    @Test
    void ensureRegistered_whenNewUser_createsCredentials() {
        when(userRepository.findByTelegramUserId(1767376980L)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("igor_dev")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        TelegramUserRegistrationService.RegistrationResult result =
                service.ensureRegistered(1767376980L, Optional.of("Igor_Dev"));

        assertThat(result.newlyRegistered()).isTrue();
        assertThat(result.username()).isEqualTo("igor_dev");
        assertThat(result.oneTimeValue()).hasSize(16);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getTelegramUserId()).isEqualTo(1767376980L);

        ArgumentCaptor<UserCredentialsEntity> credentialsCaptor = ArgumentCaptor.forClass(UserCredentialsEntity.class);
        verify(credentialsRepository).save(credentialsCaptor.capture());
        assertThat(credentialsCaptor.getValue().getPasswordHash()).isEqualTo("encoded");
    }

    @Test
    void ensureRegistered_whenUsernameTaken_usesChatIdFallback() {
        when(userRepository.findByTelegramUserId(1767376980L)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("demo")).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        TelegramUserRegistrationService.RegistrationResult result =
                service.ensureRegistered(1767376980L, Optional.of("demo"));

        assertThat(result.username()).isEqualTo("user_1767376980");
    }
}
