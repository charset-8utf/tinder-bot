package com.tinderbot.telegram.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBotApiException extends RuntimeException {

    public TelegramBotApiException(String message, TelegramApiException cause) {
        super(message, cause);
    }
}
