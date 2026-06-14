package com.tinderbot.telegram.dto;

import jakarta.validation.constraints.NotBlank;

public record AppendMessageRequest(
        @NotBlank String text
) {
}
