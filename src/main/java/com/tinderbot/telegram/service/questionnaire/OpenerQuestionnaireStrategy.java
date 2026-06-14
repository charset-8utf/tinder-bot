package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.api.session.OpenerQuestionnaireStore;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenerQuestionnaireStrategy implements QuestionnaireStrategy {

    private final OpenerQuestionnaireStore sessions;

    private final String[] questions = {
            "Имя девушки?",
            "Сколько ей лет?",
            "Есть ли у неё хобби и какие?",
            "Кем она работает?",
            "Цель знакомства?"
    };

    @Override
    public QuestionnaireType type() {
        return QuestionnaireType.OPENER;
    }

    @Override
    public DialogMode dialogMode() {
        return DialogMode.OPENER;
    }

    @Override
    public String[] questions() {
        return questions;
    }

    @Override
    public void saveAnswer(int step, UserInfo temp, String answer) {
        switch (step) {
            case 0 -> temp.setName(answer);
            case 1 -> temp.setAge(answer);
            case 2 -> temp.setHobby(answer);
            case 3 -> temp.setOccupation(answer);
            case 4 -> temp.setGoals(answer);
            default -> throw new IllegalStateException("Неожиданный шаг: " + step);
        }
    }

    @Override
    public boolean isFilled(UserInfo temp) {
        return temp != null
                && isFieldSet(temp.getName())
                && isFieldSet(temp.getAge())
                && isFieldSet(temp.getHobby())
                && isFieldSet(temp.getOccupation())
                && isFieldSet(temp.getGoals());
    }

    @Override
    public String intro(MessageView messageView) {
        return messageView.getOpenerIntro();
    }

    @Override
    public String prompt(MessageView messageView) {
        return messageView.getOpenerPrompt();
    }

    @Override
    public String photoKey() {
        return ResourceConstants.IMAGE_OPENER;
    }

    @Override
    public String thinkingMessage() {
        return "ChatGPT придумывает первое сообщение...";
    }

    @Override
    public QuestionnaireSessionOps sessionOps() {
        return new QuestionnaireSessionOps(
                sessions::getOpenerStep,
                sessions::setOpenerStep,
                sessions::getOpenerTemp,
                sessions::setOpenerTemp
        );
    }
}
