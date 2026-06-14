package com.tinderbot.telegram.core;

import org.springframework.stereotype.Component;

@Component
public class TelegramCommandNormalizer {

    public String normalize(String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return "";
        }
        String first = messageText.trim().split("\\s+", 2)[0];
        int at = first.indexOf('@');
        if (at > 0) {
            first = first.substring(0, at);
        }
        return first;
    }
}
