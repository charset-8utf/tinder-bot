package com.tinderbot.telegram.service.questionnaire;

import com.tinderbot.telegram.api.session.ProfileQuestionnaireStore;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileQuestionnaireStrategy implements QuestionnaireStrategy {

    private final ProfileQuestionnaireStore sessions;

    private final String[] questions = {
            "Сколько вам лет?",
            "Кем вы работаете?",
            "У вас есть хобби?",
            "Что вам НЕ нравится в людях?",
            "Цель знакомства?"
    };

    @Override
    public QuestionnaireType type() {
        return QuestionnaireType.PROFILE;
    }

    @Override
    public DialogMode dialogMode() {
        return DialogMode.PROFILE;
    }

    @Override
    public String[] questions() {
        return questions;
    }

    @Override
    public void saveAnswer(int step, UserInfo temp, String answer) {
        switch (step) {
            case 0 -> temp.setAge(answer);
            case 1 -> temp.setOccupation(answer);
            case 2 -> temp.setHobby(answer);
            case 3 -> temp.setAnnoys(answer);
            case 4 -> temp.setGoals(answer);
            default -> throw new IllegalStateException("Неожиданный шаг: " + step);
        }
    }

    @Override
    public boolean isFilled(UserInfo temp) {
        return temp != null
                && isFieldSet(temp.getAge())
                && isFieldSet(temp.getOccupation())
                && isFieldSet(temp.getHobby())
                && isFieldSet(temp.getAnnoys())
                && isFieldSet(temp.getGoals());
    }

    @Override
    public String intro(MessageView messageView) {
        return messageView.getProfileIntro();
    }

    @Override
    public String prompt(MessageView messageView) {
        return messageView.getProfilePrompt();
    }

    @Override
    public String photoKey() {
        return ResourceConstants.IMAGE_PROFILE;
    }

    @Override
    public String thinkingMessage() {
        return "ChatGPT создаёт ваш профиль...";
    }

    @Override
    public QuestionnaireSessionOps sessionOps() {
        return new QuestionnaireSessionOps(
                sessions::getProfileStep,
                sessions::setProfileStep,
                sessions::getProfileTemp,
                sessions::setProfileTemp
        );
    }
}
