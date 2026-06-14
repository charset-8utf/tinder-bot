package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.dto.QuestionnaireProgressResponse;
import com.tinderbot.telegram.dto.TextGenerationResponse;
import com.tinderbot.telegram.model.TextGenerationResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TextGenerationApiMapper {

    default TextGenerationResponse toResponse(TextGenerationResult result) {
        if (result.failed()) {
            return TextGenerationResponse.generationFailed();
        }
        if (result.hasEmptyHistory()) {
            return TextGenerationResponse.generationEmptyHistory();
        }
        return TextGenerationResponse.generated(result.text().orElse(""));
    }

    default QuestionnaireProgressResponse toQuestionnaireResponse(TextGenerationResult result) {
        if (result.failed()) {
            return QuestionnaireProgressResponse.generationFailed();
        }
        if (result.hasEmptyHistory()) {
            return QuestionnaireProgressResponse.generationEmptyHistory();
        }
        return QuestionnaireProgressResponse.generated(result.text().orElse(""));
    }
}
