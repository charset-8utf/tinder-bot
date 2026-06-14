package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.dto.UpdateSessionModeRequest;
import com.tinderbot.telegram.service.dialog.DialogRestService;
import com.tinderbot.telegram.service.session.SessionLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sessions", description = "Состояние Telegram-сессий пользователей")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionRestController {

    private final SessionLifecycleService sessionLifecycleService;
    private final DialogRestService dialogRestService;

    @Operation(summary = "Получить состояние сессии")
    @GetMapping("/{chatId}")
    public SessionResponse getSession(@PathVariable Long chatId) {
        return sessionLifecycleService.getSession(chatId);
    }

    @Operation(summary = "Изменить режим диалога")
    @PatchMapping("/{chatId}/mode")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMode(
            @PathVariable Long chatId,
            @Valid @RequestBody UpdateSessionModeRequest request) {
        dialogRestService.updateMode(chatId, request.mode());
    }

    @Operation(summary = "Удалить сессию")
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long chatId) {
        sessionLifecycleService.deleteSession(chatId);
    }
}
