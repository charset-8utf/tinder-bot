package com.tinderbot.telegram.common.config;

import com.tinderbot.telegram.service.auth.ApiAuthenticationService;
import com.tinderbot.telegram.service.auth.ApiUnauthorizedResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ApiAuthFilter extends OncePerRequestFilter {

    private final ApiAuthenticationService authenticationService;
    private final ApiUnauthorizedResponseWriter unauthorizedResponseWriter;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/auth/login")) {
            return true;
        }
        return !uri.startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (authenticationService.authenticateByBearer(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        unauthorizedResponseWriter.write(response);
    }
}
