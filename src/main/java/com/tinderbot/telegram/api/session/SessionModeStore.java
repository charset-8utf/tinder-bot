package com.tinderbot.telegram.api.session;

import com.tinderbot.telegram.model.DialogMode;

public interface SessionModeStore {

    DialogMode getCurrentMode(Long chatId);

    void setCurrentMode(Long chatId, DialogMode mode);
}
