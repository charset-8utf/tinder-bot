package com.tinderbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DateChatResponse(
        Status status,
        String reply,
        String typingMessage,
        Integer messagesUsed,
        Integer messagesLimit
) {

    public enum Status {
        REPLIED,
        LIMIT_EXCEEDED,
        GENERATION_FAILED
    }

    public static DateChatResponse replied(String reply, String typingMessage, int messagesUsed, int messagesLimit) {
        return new DateChatResponse(Status.REPLIED, reply, typingMessage, messagesUsed, messagesLimit);
    }

    public static DateChatResponse limitExceeded(int messagesUsed, int messagesLimit) {
        return new DateChatResponse(Status.LIMIT_EXCEEDED, null, null, messagesUsed, messagesLimit);
    }

    public static DateChatResponse generationFailed(String typingMessage, int messagesUsed, int messagesLimit) {
        return new DateChatResponse(Status.GENERATION_FAILED, null, typingMessage, messagesUsed, messagesLimit);
    }
}
