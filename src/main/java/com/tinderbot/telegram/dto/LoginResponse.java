package com.tinderbot.telegram.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
