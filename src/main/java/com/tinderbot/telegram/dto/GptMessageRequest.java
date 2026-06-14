package com.tinderbot.telegram.dto;

import jakarta.validation.constraints.NotBlank;

public record GptMessageRequest(
        @NotBlank String question
) {
}
