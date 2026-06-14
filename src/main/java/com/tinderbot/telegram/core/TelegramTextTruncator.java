package com.tinderbot.telegram.core;

import org.springframework.stereotype.Component;

@Component
public class TelegramTextTruncator {

    public static final int TELEGRAM_MESSAGE_TEXT_MAX = 4096;

    public String truncate(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= TELEGRAM_MESSAGE_TEXT_MAX) {
            return text;
        }
        return text.substring(0, TELEGRAM_MESSAGE_TEXT_MAX - 1) + "…";
    }
}
