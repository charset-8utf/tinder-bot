package com.tinderbot.telegram.api.session;

import com.tinderbot.telegram.model.UserInfo;

public interface ProfileQuestionnaireStore {

    int getProfileStep(Long chatId);

    void setProfileStep(Long chatId, int step);

    UserInfo getProfileTemp(Long chatId);

    void setProfileTemp(Long chatId, UserInfo profileTemp);
}
