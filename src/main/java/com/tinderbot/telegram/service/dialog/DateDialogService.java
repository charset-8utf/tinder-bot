package com.tinderbot.telegram.service.dialog;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.session.DateSessionStore;
import com.tinderbot.telegram.api.session.GptHistoryStore;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.common.config.StarRegistry;
import com.tinderbot.telegram.model.DateChatResult;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.Star;
import com.tinderbot.telegram.model.StarDialogStart;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateDialogService {

    private final SessionModeStore sessionModes;
    private final DateSessionStore dateSessions;
    private final GptHistoryStore gptHistory;
    private final IChatGPTService chatGPTService;
    private final MessageView messageView;
    private final TextGenerationResults generationResults;
    private final StarRegistry stars;

    public void enterDateMode(Long chatId) {
        dateSessions.setCurrentStarKey(chatId, null);
        dateSessions.resetDateMessageCount(chatId);
        gptHistory.clearChatGptHistory(chatId);
        sessionModes.setCurrentMode(chatId, DialogMode.DATE);
    }

    public Optional<StarDialogStart> startStarDialog(Long chatId, String starKey) {
        return stars.findByKey(starKey).map(star -> {
            dateSessions.resetDateMessageCount(chatId);
            gptHistory.clearChatGptHistory(chatId);
            String prompt = messageView.loadPromptByKey(star.getPromptKey());
            chatGPTService.setPrompt(chatId, prompt);
            dateSessions.setCurrentStarKey(chatId, starKey);
            sessionModes.setCurrentMode(chatId, DialogMode.DATE);

            String gender = star.isFemale() ? "девушку" : "парня";
            String instruction = "Отличный выбор! Твоя задача пригласить %s на свидание ❤️ за %d сообщений."
                    .formatted(gender, star.getMessageLimit());

            return new StarDialogStart(star, starKey, star.getPhotoKey(), prompt, instruction);
        });
    }

    public DateChatResult reply(Long chatId, String userMessage) {
        String typingMessage = Optional.ofNullable(dateSessions.getCurrentStarKey(chatId))
                .flatMap(stars::findByKey)
                .map(star -> star.getName() + " печатает...")
                .orElse("Собеседник печатает...");

        Optional<Integer> limit = Optional.ofNullable(dateSessions.getCurrentStarKey(chatId))
                .flatMap(stars::findByKey)
                .map(Star::getMessageLimit);

        if (limit.filter(l -> dateSessions.getDateMessageCount(chatId) >= l).isPresent()) {
            return new DateChatResult(true, "", generationResults.emptyHistory());
        }

        try {
            String answer = chatGPTService.addMessage(chatId, userMessage);
            dateSessions.incrementDateMessageCount(chatId);
            return new DateChatResult(false, typingMessage, generationResults.success(answer));
        } catch (Exception e) {
            log.error("Ошибка в режиме DATE: {} — {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return new DateChatResult(false, typingMessage, generationResults.failure());
        }
    }
}
