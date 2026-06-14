package com.tinderbot.telegram.model;

/**
 * Режимы диалога бота.
 */
public enum DialogMode {
    /** Главное меню. */
    MAIN,
    /** Генерация профиля. */
    PROFILE,
    /** Сообщение для знакомства. */
    OPENER,
    /** Переписка от имени пользователя. */
    MESSAGE,
    /** Переписка со звёздами. */
    DATE,
    /** Вопросы к ChatGPT. */
    GPT
}