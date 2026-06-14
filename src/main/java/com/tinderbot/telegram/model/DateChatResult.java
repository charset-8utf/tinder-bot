package com.tinderbot.telegram.model;

public record DateChatResult(boolean limitExceeded, String typingMessage, TextGenerationResult reply) {
}
