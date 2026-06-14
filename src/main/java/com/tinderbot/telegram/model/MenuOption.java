package com.tinderbot.telegram.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MenuOption {
    START("btn_start", "Главное меню бота", null),
    PROFILE("btn_profile", "Генерация Tinder-профиля \uD83D\uDE0E", "Вы выбрали: Генерация Tinder-профиля \uD83D\uDE0E"),
    OPENER("btn_opener", "Сообщение для знакомства \uD83E\uDD70", "Вы выбрали: Сообщение для знакомства \uD83E\uDD70"),
    MESSAGE("btn_message", "Переписка от вашего имени \uD83D\uDE08", "Вы выбрали: Переписка от вашего имени \uD83D\uDE08"),
    DATE("btn_date", "Переписка со звездами \uD83D\uDD25", "Вы выбрали: Переписка со звездами \uD83D\uDD25"),
    GPT("btn_gpt", "Задать вопрос чату GPT \uD83E\uDDE0", "Вы выбрали: Задать вопрос чату GPT \uD83E\uDDE0");

    private final String callback;
    private final String buttonText;
    private final String responseText;
}
