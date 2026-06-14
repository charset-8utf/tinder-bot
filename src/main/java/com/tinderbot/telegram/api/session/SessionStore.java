package com.tinderbot.telegram.api.session;

import com.tinderbot.telegram.model.UserSession;

public interface SessionStore {

    UserSession getOrCreate(Long chatId);

    void delete(Long chatId);
}
