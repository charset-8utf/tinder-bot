package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.model.QuestionnaireProgress;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final SessionModeStore sessionModes;
    private final MessageView messageView;
    private final QuestionnaireStrategyRegistry strategyRegistry;

    public QuestionnaireProgress start(Long chatId, QuestionnaireType type) {
        QuestionnaireStrategy strategy = strategyRegistry.require(type);
        reset(chatId, strategy);
        sessionModes.setCurrentMode(chatId, strategy.dialogMode());
        return new QuestionnaireProgress.Started(
                strategy.intro(messageView),
                strategy.photoKey(),
                strategy.questions()[0]
        );
    }

    public QuestionnaireProgress submitAnswer(Long chatId, QuestionnaireType type, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Пустой ответ в опроснике");
        }
        QuestionnaireStrategy strategy = strategyRegistry.require(type);
        QuestionnaireStrategy.QuestionnaireSessionOps ops = strategy.sessionOps();
        int step = ops.stepGetter().apply(chatId);
        UserInfo temp = ops.tempGetter().apply(chatId);

        strategy.saveAnswer(step, temp, answer);
        int nextStep = step + 1;
        String[] questions = strategy.questions();

        if (nextStep < questions.length) {
            ops.stepSetter().accept(chatId, nextStep);
            return new QuestionnaireProgress.NextQuestion(questions[nextStep]);
        }

        ops.stepSetter().accept(chatId, 0);
        return new QuestionnaireProgress.Completed(
                strategy.thinkingMessage(),
                strategy.prompt(messageView),
                temp.toString()
        );
    }

    public DialogMode dialogMode(QuestionnaireType type) {
        return strategyRegistry.require(type).dialogMode();
    }

    public QuestionnaireProgress.Completed completedState(Long chatId, QuestionnaireType type) {
        QuestionnaireStrategy strategy = strategyRegistry.require(type);
        UserInfo temp = strategy.sessionOps().tempGetter().apply(chatId);
        if (!strategy.isFilled(temp)) {
            throw new IllegalStateException("Опросник не заполнен");
        }
        return new QuestionnaireProgress.Completed(
                strategy.thinkingMessage(),
                strategy.prompt(messageView),
                temp.toString()
        );
    }

    private void reset(Long chatId, QuestionnaireStrategy strategy) {
        QuestionnaireStrategy.QuestionnaireSessionOps ops = strategy.sessionOps();
        ops.stepSetter().accept(chatId, 0);
        ops.tempSetter().accept(chatId, new UserInfo());
    }
}
