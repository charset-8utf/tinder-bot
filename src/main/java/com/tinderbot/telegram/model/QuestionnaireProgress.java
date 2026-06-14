package com.tinderbot.telegram.model;

/**
 * Состояние опросника после старта или ответа пользователя.
 */
public sealed interface QuestionnaireProgress {

    record Started(String intro, String photoKey, String firstQuestion) implements QuestionnaireProgress {}

    record NextQuestion(String question) implements QuestionnaireProgress {}

    record Completed(String thinkingMessage, String prompt, String userData) implements QuestionnaireProgress {}
}
