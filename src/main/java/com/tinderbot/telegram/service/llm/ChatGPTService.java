package com.tinderbot.telegram.service.llm;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.session.GptHistoryStore;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Slf4j
@Service
@ConditionalOnBean(ChatGPT.class)
@RequiredArgsConstructor
public class ChatGPTService implements IChatGPTService {

    private final ChatGptCompletionClient completionClient;
    private final GptHistoryStore gptHistory;

    @PostConstruct
    void logReady() {
        log.info("ChatGPTService готов к работе");
    }

    @Override
    public String sendMessage(Long chatId, String prompt, String question) {
        log.debug("Отправка запроса к ChatGPT: prompt={}, question={}", prompt, question);
        List<Message> messageHistory = Stream.of(
                        Message.ofSystem(prompt),
                        Message.of(question)
                )
                .toList();
        gptHistory.setChatGptHistory(chatId, messageHistory);
        return sendMessagesToChatGPT(chatId, messageHistory);
    }

    @Override
    @Async
    public CompletableFuture<String> sendMessageAsync(Long chatId, String prompt, String question) {
        try {
            String answer = sendMessage(chatId, prompt, question);
            return CompletableFuture.completedFuture(answer);
        } catch (Exception e) {
            log.error("Ошибка при асинхронном вызове ChatGPT", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void setPrompt(Long chatId, String prompt) {
        log.debug("Установка системного промпта: {}", prompt);
        Message system = Message.ofSystem(prompt);
        gptHistory.setChatGptHistory(chatId, new ArrayList<>(List.of(system)));
    }

    @Override
    public String addMessage(Long chatId, String question) {
        log.debug("Добавление сообщения в историю: {}", question);
        List<Message> historySnapshot = gptHistory.getChatGptHistory(chatId);
        List<Message> messageHistory = Stream.concat(
                        historySnapshot.isEmpty()
                                ? Stream.of(Message.ofSystem("You are a helpful assistant."))
                                : historySnapshot.stream(),
                        Stream.of(Message.of(question))
                )
                .toList();
        gptHistory.setChatGptHistory(chatId, messageHistory);
        return sendMessagesToChatGPT(chatId, messageHistory);
    }

    private String sendMessagesToChatGPT(Long chatId, List<Message> messageHistory) {
        String content = completionClient.complete(messageHistory);

        List<Message> updatedHistory = Stream.concat(
                        messageHistory.stream(),
                        Stream.of(Message.ofAssistant(content))
                )
                .toList();
        gptHistory.setChatGptHistory(chatId, updatedHistory);

        log.debug("Получен ответ от ChatGPT (длина {} символов)", content.length());
        return content;
    }
}
