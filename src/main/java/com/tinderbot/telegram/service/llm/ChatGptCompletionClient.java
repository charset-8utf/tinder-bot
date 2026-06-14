package com.tinderbot.telegram.service.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.tinderbot.telegram.common.config.OpenAiApiHostResolver;
import com.tinderbot.telegram.exception.ChatGptCompletionException;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.Message;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@ConditionalOnBean(ChatGPT.class)
public class ChatGptCompletionClient {

    private static final int RESPONSE_LOG_SNIPPET_MAX_LENGTH = 2000;
    private static final long SLOW_RESPONSE_LOG_THRESHOLD_SECONDS = 60;

    private final JsonMapper objectMapper;
    private final OpenAiApiHostResolver apiHostResolver;
    private final OpenAiChatCompletionParser completionParser;
    private final String apiKey;
    private final String configuredApiHost;
    private final String model;
    private final int maxTokens;
    private final int readTimeoutSeconds;

    public ChatGptCompletionClient(
            JsonMapper objectMapper,
            OpenAiApiHostResolver apiHostResolver,
            OpenAiChatCompletionParser completionParser,
            @Value("${openai.token}") String apiKey,
            @Value("${openai.api-host}") String configuredApiHost,
            @Value("${openai.model:gpt-3.5-turbo}") String model,
            @Value("${openai.max-tokens:8192}") int maxTokens,
            @Value("${openai.read-timeout-seconds:1200}") int readTimeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiHostResolver = apiHostResolver;
        this.completionParser = completionParser;
        this.apiKey = apiKey;
        this.configuredApiHost = configuredApiHost;
        this.model = model;
        this.maxTokens = maxTokens;
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    private RestClient restClient;
    private String completionsUrl;

    @PostConstruct
    void initRestClient() {
        this.completionsUrl = apiHostResolver.resolve(configuredApiHost) + "v1/chat/completions";

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(45))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        log.info("LLM HTTP client: readTimeout={}s, completionsUrl={}", readTimeoutSeconds, completionsUrl);
    }

    @Retry(name = "chatGpt")
    public String complete(List<Message> messages) {
        long startedAt = System.nanoTime();
        try {
            ObjectNode body = buildRequestBody(messages);
            String raw = fetchCompletionRaw(body);
            JsonNode root = objectMapper.readTree(raw);
            JsonNode messageNode = root.path("choices").path(0).path("message");

            String reply = completionParser.assistantReply(root);
            validateReply(reply, raw, messageNode);
            logSlowResponseIfNeeded(startedAt, raw);
            return reply;
        } catch (RestClientException e) {
            throw wrapLlmError("Ошибка HTTP при вызове LLM", e);
        } catch (JacksonException e) {
            throw wrapLlmError("Не удалось сериализовать или разобрать ответ LLM", e);
        }
    }

    private ObjectNode buildRequestBody(List<Message> messages) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.9);
        body.put("max_tokens", maxTokens);
        ArrayNode arr = body.putArray("messages");
        for (Message message : messages) {
            if (message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            ObjectNode one = arr.addObject();
            one.put("role", message.getRole());
            one.put(OpenAiChatCompletionParser.FIELD_CONTENT, message.getContent());
        }
        if (arr.isEmpty()) {
            throw new ChatGptCompletionException("Нет сообщений с непустым content для Chat Completions");
        }
        return body;
    }

    private String fetchCompletionRaw(ObjectNode body) {
        String raw = restClient.post()
                .uri(completionsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(objectMapper.writeValueAsString(body))
                .retrieve()
                .body(String.class);
        if (raw == null) {
            throw new ChatGptCompletionException("Пустой HTTP-ответ от LLM");
        }
        return raw;
    }

    private void validateReply(String reply, String raw, JsonNode messageNode) {
        if (reply.isBlank()) {
            log.error("Пустой ответ модели (нет content и reasoning): {}", truncateRaw(raw));
            throw new ChatGptCompletionException("Модель вернула пустой ответ (content и reasoning пусты)");
        }
        if (completionParser.isFieldBlank(messageNode, OpenAiChatCompletionParser.FIELD_CONTENT)
                && completionParser.hasNonBlankField(messageNode, OpenAiChatCompletionParser.FIELD_REASONING_CONTENT)) {
            log.warn("Модель вернула пустой content и заполнила reasoning_content — в чат отправлен последний блок рассуждения.");
        }
    }

    private void logSlowResponseIfNeeded(long startedAt, String raw) {
        long seconds = Duration.ofNanos(System.nanoTime() - startedAt).toSeconds();
        if (seconds >= SLOW_RESPONSE_LOG_THRESHOLD_SECONDS) {
            log.info("LLM chat/completions завершился за {} с (длина JSON ответа {} символов)", seconds, raw.length());
        }
    }

    private String truncateRaw(String raw) {
        return raw.length() > RESPONSE_LOG_SNIPPET_MAX_LENGTH
                ? raw.substring(0, RESPONSE_LOG_SNIPPET_MAX_LENGTH) + "…"
                : raw;
    }

    private ChatGptCompletionException wrapLlmError(String message, Exception exception) {
        logLlmError(exception);
        return new ChatGptCompletionException(message, exception);
    }

    private void logLlmError(Exception exception) {
        log.error("Ошибка вызова LLM (модель {}). Проверьте OPENAI_API_HOST, ключ и лимиты провайдера.", model, exception);
    }
}
