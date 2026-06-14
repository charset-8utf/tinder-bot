package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.dto.QuestionnaireAnswerRequest;
import com.tinderbot.telegram.dto.QuestionnaireProgressResponse;
import com.tinderbot.telegram.mapper.QuestionnaireApiMapper;
import com.tinderbot.telegram.mapper.TextGenerationApiMapper;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireGenerationService;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireService;
import com.tinderbot.telegram.service.session.RestSessionAccessService;
import com.tinderbot.telegram.model.QuestionnaireProgress;
import com.tinderbot.telegram.model.QuestionnaireType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Questionnaires", description = "Опросники профиля и opener")
@RestController
@RequestMapping("/api/v1/sessions/{chatId}/questionnaires/{type}")
@RequiredArgsConstructor
public class QuestionnaireRestController {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireGenerationService generationService;
    private final QuestionnaireApiMapper questionnaireApiMapper;
    private final TextGenerationApiMapper textGenerationApiMapper;
    private final RestSessionAccessService sessionAccessService;

    @Operation(summary = "Начать опросник")
    @PostMapping("/start")
    public QuestionnaireProgressResponse start(
            @PathVariable Long chatId,
            @PathVariable QuestionnaireType type) {
        sessionAccessService.ensureCanAccessSession(chatId);
        QuestionnaireProgress progress = questionnaireService.start(chatId, type);
        return questionnaireApiMapper.toResponse(progress);
    }

    @Operation(summary = "Отправить ответ на текущий вопрос")
    @PostMapping("/answers")
    public QuestionnaireProgressResponse submitAnswer(
            @PathVariable Long chatId,
            @PathVariable QuestionnaireType type,
            @Valid @RequestBody QuestionnaireAnswerRequest request) {
        sessionAccessService.ensureCanAccessSession(chatId);
        QuestionnaireProgress progress = questionnaireService.submitAnswer(chatId, type, request.answer());
        return questionnaireApiMapper.toResponse(progress);
    }

    @Operation(summary = "Сгенерировать текст по завершённому опроснику")
    @PostMapping("/generate")
    public QuestionnaireProgressResponse generate(
            @PathVariable Long chatId,
            @PathVariable QuestionnaireType type) {
        sessionAccessService.ensureCanAccessSession(chatId);
        QuestionnaireProgress.Completed completed = questionnaireService.completedState(chatId, type);
        var result = generationService.generate(chatId, type, completed.prompt(), completed.userData());
        return textGenerationApiMapper.toQuestionnaireResponse(result);
    }
}
