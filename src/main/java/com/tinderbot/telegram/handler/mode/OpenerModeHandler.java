package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireGenerationService;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireService;
import com.tinderbot.telegram.model.QuestionnaireType;
import org.springframework.stereotype.Component;

@Component
public class OpenerModeHandler extends QuestionnaireModeHandler {

    public OpenerModeHandler(TelegramUiSessionStore telegramUi,
                             IMessageCleaner messageCleaner,
                             MessageSender messageSender,
                             QuestionnaireService questionnaireService,
                             QuestionnaireGenerationService generationService) {
        super(telegramUi, messageCleaner, messageSender, questionnaireService, generationService);
    }

    @Override
    protected QuestionnaireType getQuestionnaireType() {
        return QuestionnaireType.OPENER;
    }
}
