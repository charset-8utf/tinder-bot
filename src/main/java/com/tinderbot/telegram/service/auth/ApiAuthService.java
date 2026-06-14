package com.tinderbot.telegram.service.auth;

import com.tinderbot.telegram.dto.LoginResponse;
import com.tinderbot.telegram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<LoginResponse> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameWithCredentials(username.trim())
                .filter(user -> passwordEncoder.matches(password, user.getCredentials().getPasswordHash()))
                .map(user -> new LoginResponse(jwtService.createAccessToken(user.getUsername()), "Bearer"));
    }
}
