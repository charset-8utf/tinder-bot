package com.tinderbot.telegram.integration;

import com.tinderbot.telegram.api.IChatGPTService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.CompletableFuture;

@TestConfiguration
public class StubChatGptTestConfiguration {

    public static final String STUB_GENERATED_TEXT = "Stub generated text for integration tests";

    @Bean
    @Primary
    IChatGPTService stubChatGptService() {
        return new IChatGPTService() {
            @Override
            public String sendMessage(Long chatId, String prompt, String question) {
                return STUB_GENERATED_TEXT;
            }

            @Override
            public CompletableFuture<String> sendMessageAsync(Long chatId, String prompt, String question) {
                return CompletableFuture.completedFuture(STUB_GENERATED_TEXT);
            }

            @Override
            public void setPrompt(Long chatId, String prompt) {
                // no-op
            }

            @Override
            public String addMessage(Long chatId, String question) {
                return STUB_GENERATED_TEXT;
            }
        };
    }
}
