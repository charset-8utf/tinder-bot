package com.tinderbot.telegram.model;

public record DateMessageResult(
        DateChatResult chatResult,
        int messagesUsed,
        int messagesLimit
) {
}
