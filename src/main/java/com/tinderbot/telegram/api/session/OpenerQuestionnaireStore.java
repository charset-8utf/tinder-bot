package com.tinderbot.telegram.api.session;

import com.tinderbot.telegram.model.UserInfo;

public interface OpenerQuestionnaireStore {

    int getOpenerStep(Long chatId);

    void setOpenerStep(Long chatId, int step);

    UserInfo getOpenerTemp(Long chatId);

    void setOpenerTemp(Long chatId, UserInfo openerTemp);
}
