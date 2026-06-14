package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.common.config.ApiSecurityProperties;
import com.tinderbot.telegram.model.ApiPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RestSessionAccessService {

    private final ApiSecurityProperties securityProperties;

    public void ensureCanAccessSession(Long chatId) {
        if (!securityProperties.enabled()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ApiPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещён");
        }
        if (principal.telegramUserId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Telegram-аккаунт не привязан");
        }
        if (!principal.telegramUserId().equals(chatId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ к сессии " + chatId + " запрещён");
        }
    }
}
