package com.tinderbot.telegram.testsupport;

import com.tinderbot.telegram.service.questionnaire.QuestionnaireGenerationService;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireService;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import com.tinderbot.telegram.service.questionnaire.OpenerQuestionnaireStrategy;
import com.tinderbot.telegram.service.questionnaire.ProfileQuestionnaireStrategy;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireStrategyRegistry;
import com.tinderbot.telegram.view.MessageView;
import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.ISessionService;

import java.util.List;

public final class QuestionnaireTestFixtures {

    private QuestionnaireTestFixtures() {
    }

    public static QuestionnaireService questionnaireService(ISessionService sessionService, MessageView messageView) {
        return new QuestionnaireService(sessionService, messageView, strategyRegistry(sessionService));
    }

    public static QuestionnaireGenerationService generationService(IChatGPTService chatGPTService) {
        return new QuestionnaireGenerationService(chatGPTService, new TextGenerationResults());
    }

    public static QuestionnaireStrategyRegistry strategyRegistry(ISessionService sessionService) {
        return new QuestionnaireStrategyRegistry(List.of(
                new ProfileQuestionnaireStrategy(sessionService),
                new OpenerQuestionnaireStrategy(sessionService)
        ));
    }
}
