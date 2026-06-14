package com.tinderbot.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionnaireProgressResponse(
        Status status,
        String intro,
        String photoKey,
        String question,
        String thinkingMessage,
        String prompt,
        String userData,
        String generatedText
) {

    public enum Status {
        STARTED,
        NEXT_QUESTION,
        COMPLETED,
        GENERATED,
        GENERATION_FAILED,
        GENERATION_EMPTY_HISTORY
    }

    public static QuestionnaireProgressResponse started(String intro, String photoKey, String firstQuestion) {
        return new QuestionnaireProgressResponse(
                Status.STARTED, intro, photoKey, firstQuestion, null, null, null, null);
    }

    public static QuestionnaireProgressResponse nextQuestion(String question) {
        return new QuestionnaireProgressResponse(
                Status.NEXT_QUESTION, null, null, question, null, null, null, null);
    }

    public static QuestionnaireProgressResponse completed(String thinkingMessage, String prompt, String userData) {
        return new QuestionnaireProgressResponse(
                Status.COMPLETED, null, null, null, thinkingMessage, prompt, userData, null);
    }

    public static QuestionnaireProgressResponse generated(String text) {
        return new QuestionnaireProgressResponse(
                Status.GENERATED, null, null, null, null, null, null, text);
    }

    public static QuestionnaireProgressResponse generationFailed() {
        return new QuestionnaireProgressResponse(
                Status.GENERATION_FAILED, null, null, null, null, null, null, null);
    }

    public static QuestionnaireProgressResponse generationEmptyHistory() {
        return new QuestionnaireProgressResponse(
                Status.GENERATION_EMPTY_HISTORY, null, null, null, null, null, null, null);
    }
}
