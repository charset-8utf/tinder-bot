package com.tinderbot.telegram.api;

import com.tinderbot.telegram.api.session.DateSessionStore;
import com.tinderbot.telegram.api.session.GptHistoryStore;
import com.tinderbot.telegram.api.session.MessageHistoryStore;
import com.tinderbot.telegram.api.session.OpenerQuestionnaireStore;
import com.tinderbot.telegram.api.session.ProfileQuestionnaireStore;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.api.session.SessionStore;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;

public interface ISessionService extends
        SessionStore,
        SessionModeStore,
        TelegramUiSessionStore,
        ProfileQuestionnaireStore,
        OpenerQuestionnaireStore,
        MessageHistoryStore,
        GptHistoryStore,
        DateSessionStore {
}
