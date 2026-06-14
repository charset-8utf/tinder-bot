package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.dto.AppendMessageRequest;
import com.tinderbot.telegram.dto.DateChatResponse;
import com.tinderbot.telegram.dto.DateMessageRequest;
import com.tinderbot.telegram.dto.GptMessageRequest;
import com.tinderbot.telegram.dto.TextGenerationResponse;
import com.tinderbot.telegram.mapper.DateChatApiMapper;
import com.tinderbot.telegram.mapper.TextGenerationApiMapper;
import com.tinderbot.telegram.service.dialog.DialogRestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@Tag(name = "Dialogs", description = "GPT-диалог и генерация сообщений для переписки")
@RestController
@RequestMapping("/api/v1/sessions/{chatId}")
@RequiredArgsConstructor
public class DialogRestController {

    private final DialogRestService dialogRestService;
    private final TextGenerationApiMapper textGenerationApiMapper;
    private final DateChatApiMapper dateChatApiMapper;

    @Operation(summary = "Задать вопрос ChatGPT")
    @PostMapping("/gpt/messages")
    public TextGenerationResponse askGpt(
            @PathVariable Long chatId,
            @Valid @RequestBody GptMessageRequest request) {
        return textGenerationApiMapper.toResponse(
                dialogRestService.askGpt(chatId, request.question()));
    }

    @Operation(summary = "Добавить сообщение в историю переписки")
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void appendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody AppendMessageRequest request) {
        dialogRestService.appendMessage(chatId, request.text());
    }

    @Operation(summary = "Сгенерировать следующее сообщение по истории переписки")
    @PostMapping("/messages/next")
    public TextGenerationResponse generateNextMessage(@PathVariable Long chatId) {
        return textGenerationApiMapper.toResponse(dialogRestService.generateNextMessage(chatId));
    }

    @Operation(summary = "Отправить сообщение звезде в режиме DATE")
    @PostMapping("/date/messages")
    public DateChatResponse sendDateMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody DateMessageRequest request) {
        return dateChatApiMapper.toResponse(
                dialogRestService.sendDateMessage(chatId, request.message(), request.starKey()));
    }
}
