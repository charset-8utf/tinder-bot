package com.tinderbot.telegram.service.auth;

import com.tinderbot.telegram.model.ApiPrincipal;
import com.tinderbot.telegram.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiAuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public boolean authenticateByBearer(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return jwtService.parseAuthorizationHeader(authorizationHeader)
                .filter(JwtService.ParsedAuthorizationHeader::jwtFormat)
                .flatMap(parsed -> jwtService.validateAuthorizationHeader(authorizationHeader))
                .flatMap(userRepository::findByUsernameWithCredentials)
                .map(user -> {
                    setAuthentication(new ApiPrincipal(user.getUsername(), user.getTelegramUserId()));
                    return true;
                })
                .orElse(false);
    }

    private void setAuthentication(ApiPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        "JWT",
                        List.of(new SimpleGrantedAuthority("ROLE_API"))));
    }
}
