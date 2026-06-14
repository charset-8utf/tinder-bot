package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.view.MessageView;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface QuestionnaireStrategy {

    QuestionnaireType type();

    DialogMode dialogMode();

    String[] questions();

    void saveAnswer(int step, UserInfo temp, String answer);

    boolean isFilled(UserInfo temp);

    default boolean isFieldSet(String value) {
        return value != null && !value.isBlank();
    }

    String intro(MessageView messageView);

    String prompt(MessageView messageView);

    String photoKey();

    String thinkingMessage();

    QuestionnaireSessionOps sessionOps();

    record QuestionnaireSessionOps(
            Function<Long, Integer> stepGetter,
            BiConsumer<Long, Integer> stepSetter,
            Function<Long, UserInfo> tempGetter,
            BiConsumer<Long, UserInfo> tempSetter
    ) {}
}
