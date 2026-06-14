package com.tinderbot.telegram.service.telegram;

import com.tinderbot.telegram.api.session.MessageHistoryStore;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.model.DialogMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainMenuService {

    private final SessionModeStore sessionModes;
    private final MessageHistoryStore messageHistory;

    public void openMainMenu(Long chatId) {
        messageHistory.clearMessageHistory(chatId);
        sessionModes.setCurrentMode(chatId, DialogMode.MAIN);
    }
}
