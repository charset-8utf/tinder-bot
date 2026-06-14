package com.tinderbot.telegram.api;

import java.util.concurrent.CompletableFuture;

public interface IChatGPTService {
    String sendMessage(Long chatId, String prompt, String question);
    CompletableFuture<String> sendMessageAsync(Long chatId, String prompt, String question);
    void setPrompt(Long chatId, String prompt);
    String addMessage(Long chatId, String question);
}