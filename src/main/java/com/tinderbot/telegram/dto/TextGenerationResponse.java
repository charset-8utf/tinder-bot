package com.tinderbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextGenerationResponse(
        Status status,
        String generatedText
) {

    public enum Status {
        GENERATED,
        GENERATION_FAILED,
        GENERATION_EMPTY_HISTORY
    }

    public static TextGenerationResponse generated(String text) {
        return new TextGenerationResponse(Status.GENERATED, text);
    }

    public static TextGenerationResponse generationFailed() {
        return new TextGenerationResponse(Status.GENERATION_FAILED, null);
    }

    public static TextGenerationResponse generationEmptyHistory() {
        return new TextGenerationResponse(Status.GENERATION_EMPTY_HISTORY, null);
    }
}
