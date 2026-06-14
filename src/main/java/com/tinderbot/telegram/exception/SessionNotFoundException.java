package com.tinderbot.telegram.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(Long chatId) {
        super("Сессия не найдена: " + chatId);
    }
}
