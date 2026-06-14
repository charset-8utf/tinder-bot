package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.common.config.ApiSecurityProperties;
import com.tinderbot.telegram.model.ApiPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestSessionAccessServiceTest {

    private RestSessionAccessService sessionAccessService;

    @BeforeEach
    void setUp() {
        sessionAccessService = new RestSessionAccessService(new ApiSecurityProperties(true));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtPrincipal_canAccessLinkedSessionOnly() {
        setPrincipal(new ApiPrincipal("demo", 1L));

        assertThatCode(() -> sessionAccessService.ensureCanAccessSession(1L))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> sessionAccessService.ensureCanAccessSession(42L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Доступ к сессии 42 запрещён");
    }

    @Test
    void jwtPrincipal_withoutTelegramLink_isForbidden() {
        setPrincipal(new ApiPrincipal("demo", null));

        assertThatThrownBy(() -> sessionAccessService.ensureCanAccessSession(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Telegram-аккаунт не привязан");
    }

    private static void setPrincipal(ApiPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "JWT"));
    }
}
