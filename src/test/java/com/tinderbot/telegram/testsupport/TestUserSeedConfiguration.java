package com.tinderbot.telegram.testsupport;

import com.tinderbot.telegram.entity.UserCredentialsEntity;
import com.tinderbot.telegram.entity.UserEntity;
import com.tinderbot.telegram.repository.UserCredentialsRepository;
import com.tinderbot.telegram.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("test")
public class TestUserSeedConfiguration {

    @Bean
    ApplicationRunner seedDemoUser(
            UserRepository userRepository,
            UserCredentialsRepository credentialsRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsernameWithCredentials("demo").isPresent()) {
                return;
            }
            UserEntity user = new UserEntity();
            user.setUsername("demo");
            user.setTelegramUserId(1L);
            user = userRepository.save(user);

            UserCredentialsEntity credentials = new UserCredentialsEntity();
            credentials.setUser(user);
            credentials.setPasswordHash(passwordEncoder.encode("password"));
            credentialsRepository.save(credentials);
        };
    }
}
