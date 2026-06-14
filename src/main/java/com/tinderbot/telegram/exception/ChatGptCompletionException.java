package com.tinderbot.telegram.exception;

public class ChatGptCompletionException extends RuntimeException {

    public ChatGptCompletionException(String message) {
        super(message);
    }

    public ChatGptCompletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
