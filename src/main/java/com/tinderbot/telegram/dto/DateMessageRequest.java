package com.tinderbot.telegram.dto;

import jakarta.validation.constraints.NotBlank;

public record DateMessageRequest(
        @NotBlank String message,
        String starKey
) {
}
