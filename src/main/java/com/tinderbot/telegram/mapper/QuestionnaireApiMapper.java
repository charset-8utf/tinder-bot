package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.dto.QuestionnaireProgressResponse;
import com.tinderbot.telegram.model.QuestionnaireProgress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionnaireApiMapper {

    default QuestionnaireProgressResponse toResponse(QuestionnaireProgress progress) {
        return switch (progress) {
            case QuestionnaireProgress.Started(var intro, var photoKey, var firstQuestion) ->
                    QuestionnaireProgressResponse.started(intro, photoKey, firstQuestion);
            case QuestionnaireProgress.NextQuestion(var question) ->
                    QuestionnaireProgressResponse.nextQuestion(question);
            case QuestionnaireProgress.Completed(var thinkingMessage, var prompt, var userData) ->
                    QuestionnaireProgressResponse.completed(thinkingMessage, prompt, userData);
        };
    }
}
