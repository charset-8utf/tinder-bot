package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.model.TextGenerationResult;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireGenerationService {

    private final IChatGPTService chatGPTService;
    private final TextGenerationResults generationResults;

    @RateLimiter(name = "textGeneration")
    public TextGenerationResult generate(Long chatId, QuestionnaireType type, String prompt, String userData) {
        try {
            String answer = chatGPTService.sendMessage(chatId, prompt, userData);
            return generationResults.success(answer);
        } catch (Exception e) {
            log.error("Ошибка при генерации результата в режиме {}", type, e);
            return generationResults.failure();
        }
    }
}
