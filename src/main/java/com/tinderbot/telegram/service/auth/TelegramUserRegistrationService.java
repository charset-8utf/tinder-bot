package com.tinderbot.telegram.service.auth;

import com.tinderbot.telegram.entity.UserCredentialsEntity;
import com.tinderbot.telegram.entity.UserEntity;
import com.tinderbot.telegram.repository.UserCredentialsRepository;
import com.tinderbot.telegram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserRegistrationService {

    private static final int RANDOM_BYTE_COUNT = 12;

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public record RegistrationResult(boolean newlyRegistered, String username, String oneTimeValue) {
    }

    @Transactional
    public RegistrationResult ensureRegistered(Long telegramUserId, Optional<String> telegramUsername) {
        Optional<UserEntity> existing = userRepository.findByTelegramUserId(telegramUserId);
        if (existing.isPresent()) {
            return new RegistrationResult(false, existing.get().getUsername(), null);
        }

        String username = resolveUsername(telegramUserId, telegramUsername);
        String oneTimeValue = generateOneTimeValue();

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setTelegramUserId(telegramUserId);
        user = userRepository.save(user);

        UserCredentialsEntity credentials = new UserCredentialsEntity();
        credentials.setUser(user);
        credentials.setPasswordHash(passwordEncoder.encode(oneTimeValue));
        credentialsRepository.save(credentials);

        logRegistration(username, telegramUserId, oneTimeValue);

        return new RegistrationResult(true, username, oneTimeValue);
    }

    private void logRegistration(String username, Long telegramUserId, String oneTimeValue) {
        log.info(
                "Создан REST API пользователь: username={} chatId={} oneTimeValue={} "
                        + "(однократная запись в лог; POST /api/v1/auth/login, затем GET /api/v1/sessions/{})",
                username,
                telegramUserId,
                oneTimeValue,
                telegramUserId);
    }

    private String resolveUsername(Long telegramUserId, Optional<String> telegramUsername) {
        String preferred = telegramUsername
                .map(this::sanitizeUsername)
                .filter(username -> !username.isBlank())
                .orElse("user_" + telegramUserId);

        if (!userRepository.existsByUsername(preferred)) {
            return preferred;
        }
        return "user_" + telegramUserId;
    }

    private String sanitizeUsername(String raw) {
        String normalized = raw.trim().toLowerCase();
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length() && builder.length() < 64; i++) {
            char ch = normalized.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_') {
                builder.append(ch);
            }
        }
        String username = builder.toString();
        if (username.isBlank()) {
            return "";
        }
        if (Character.isDigit(username.charAt(0))) {
            return "tg_" + username;
        }
        return username;
    }

    private String generateOneTimeValue() {
        byte[] randomBytes = new byte[RANDOM_BYTE_COUNT];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
