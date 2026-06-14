package com.tinderbot.telegram.service.llm;

import com.tinderbot.telegram.api.IChatGPTService;
import com.plexpt.chatgpt.ChatGPT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnMissingBean(ChatGPT.class)
public class DisabledChatGptService implements IChatGPTService {

    private static final String HINT =
            "ИИ не настроен: задайте OPENAI_TOKEN в .env (или переменную окружения) и перезапустите контейнер.";

    @Override
    public String sendMessage(Long chatId, String prompt, String question) {
        log.warn("Вызов ChatGPT без настроенного OPENAI_TOKEN");
        return HINT;
    }

    @Override
    @Async
    public CompletableFuture<String> sendMessageAsync(Long chatId, String prompt, String question) {
        return CompletableFuture.completedFuture(sendMessage(chatId, prompt, question));
    }

    @Override
    public void setPrompt(Long chatId, String prompt) {
        // no-op
    }

    @Override
    public String addMessage(Long chatId, String question) {
        return sendMessage(chatId, null, question);
    }
}
