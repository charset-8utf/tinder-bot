package com.tinderbot.telegram.service.dialog;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.session.MessageHistoryStore;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.TextGenerationResult;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDialogService {

    private final SessionModeStore sessionModes;
    private final MessageHistoryStore messageHistory;
    private final IChatGPTService chatGPTService;
    private final MessageView messageView;
    private final TextGenerationResults generationResults;

    public void enterMessageMode(Long chatId) {
        messageHistory.clearMessageHistory(chatId);
        sessionModes.setCurrentMode(chatId, DialogMode.MESSAGE);
    }

    public void appendUserMessage(Long chatId, String text) {
        messageHistory.addMessageToHistory(chatId, text);
    }

    public TextGenerationResult generateNextMessage(Long chatId) {
        return generate(chatId, messageView.getMessagePrompt(), "next message");
    }

    public TextGenerationResult generateDateInvite(Long chatId) {
        return generate(chatId, messageView.getMessageDatePrompt(), "date invite");
    }

    private TextGenerationResult generate(Long chatId, String prompt, String logContext) {
        List<String> history = messageHistory.getMessageHistory(chatId);
        if (history.isEmpty()) {
            return generationResults.emptyHistory();
        }
        try {
            String conversation = String.join("\n", history);
            String answer = chatGPTService.sendMessage(chatId, prompt, conversation);
            return generationResults.success(answer);
        } catch (Exception e) {
            log.error("Ошибка при генерации текста ({})", logContext, e);
            return generationResults.failure();
        }
    }
}
