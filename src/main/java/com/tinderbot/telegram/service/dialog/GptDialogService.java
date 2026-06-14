package com.tinderbot.telegram.service.dialog;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.TextGenerationResult;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptDialogService {

    private final SessionModeStore sessionModes;
    private final IChatGPTService chatGPTService;
    private final MessageView messageView;
    private final TextGenerationResults generationResults;

    public void enterGptMode(Long chatId) {
        sessionModes.setCurrentMode(chatId, DialogMode.GPT);
    }

    public CompletableFuture<TextGenerationResult> ask(Long chatId, String question) {
        return chatGPTService.sendMessageAsync(chatId, messageView.getGptPrompt(), question)
                .thenApply(generationResults::success)
                .exceptionally(ex -> {
                    log.error("Ошибка при вызове ChatGPT", ex);
                    return generationResults.failure();
                });
    }
}
