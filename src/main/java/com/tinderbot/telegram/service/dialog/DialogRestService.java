package com.tinderbot.telegram.service.dialog;

import com.tinderbot.telegram.api.session.DateSessionStore;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.common.config.StarRegistry;
import com.tinderbot.telegram.model.*;
import com.tinderbot.telegram.service.session.RestSessionAccessService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogRestService {

    private final SessionModeStore sessionModes;
    private final DateSessionStore dateSessions;
    private final GptDialogService gptDialogService;
    private final MessageDialogService messageDialogService;
    private final DateDialogService dateDialogService;
    private final RestSessionAccessService sessionAccessService;
    private final StarRegistry stars;

    public void updateMode(Long chatId, DialogMode mode) {
        sessionAccessService.ensureCanAccessSession(chatId);
        sessionModes.setCurrentMode(chatId, mode);
    }

    public void appendMessage(Long chatId, String text) {
        sessionAccessService.ensureCanAccessSession(chatId);
        ensureMessageMode(chatId);
        messageDialogService.appendUserMessage(chatId, text);
    }

    @RateLimiter(name = "textGeneration")
    public TextGenerationResult askGpt(Long chatId, String question) {
        sessionAccessService.ensureCanAccessSession(chatId);
        gptDialogService.enterGptMode(chatId);
        return gptDialogService.ask(chatId, question).join();
    }

    @RateLimiter(name = "textGeneration")
    public TextGenerationResult generateNextMessage(Long chatId) {
        sessionAccessService.ensureCanAccessSession(chatId);
        ensureMessageMode(chatId);
        return messageDialogService.generateNextMessage(chatId);
    }

    @RateLimiter(name = "textGeneration")
    public DateMessageResult sendDateMessage(Long chatId, String message, String starKey) {
        sessionAccessService.ensureCanAccessSession(chatId);
        if (starKey != null && !starKey.isBlank()) {
            dateDialogService.startStarDialog(chatId, starKey)
                    .orElseThrow(() -> new IllegalArgumentException("Неизвестная звезда: " + starKey));
        } else if (dateSessions.getCurrentStarKey(chatId) == null) {
            throw new IllegalArgumentException("Укажите starKey для первого сообщения в режиме DATE");
        } else if (sessionModes.getCurrentMode(chatId) != DialogMode.DATE) {
            sessionModes.setCurrentMode(chatId, DialogMode.DATE);
        }

        DateChatResult chatResult = dateDialogService.reply(chatId, message);
        int messagesUsed = dateSessions.getDateMessageCount(chatId);
        int messagesLimit = stars.findByKey(dateSessions.getCurrentStarKey(chatId))
                .map(Star::getMessageLimit)
                .orElse(0);
        return new DateMessageResult(chatResult, messagesUsed, messagesLimit);
    }

    private void ensureMessageMode(Long chatId) {
        if (sessionModes.getCurrentMode(chatId) != DialogMode.MESSAGE) {
            messageDialogService.enterMessageMode(chatId);
        }
    }
}
