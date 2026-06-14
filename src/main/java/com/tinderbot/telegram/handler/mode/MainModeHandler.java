package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.telegram.MainMenuService;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainModeHandler implements com.tinderbot.telegram.api.ModeHandler {

    private final IMessageCleaner messageCleaner;
    private final MessageView messageView;
    private final KeyboardFactory keyboardFactory;
    private final MessageSender messageSender;
    private final MainMenuService mainMenuService;

    @Override
    public DialogMode getMode() {
        return DialogMode.MAIN;
    }

    @Override
    public void onCommand(MultiSessionTelegramBot bot, Long chatId) {
        messageCleaner.deleteAllMessages(chatId, bot);
        mainMenuService.openMainMenu(chatId);

        messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_MAIN);
        messageSender.sendAndSaveText(bot, chatId, messageView.getWelcomeText());
        messageSender.sendAndSaveMenu(bot, chatId, messageView.getMenuTitle(), keyboardFactory.createMainMenuButtons());
    }

    @Override
    public void onMessage(MultiSessionTelegramBot bot, Long chatId, String text) {
        messageCleaner.deleteCurrentMenu(chatId, bot);
        bot.sendTextMessage(messageView.getHelpMessage());
        messageSender.sendAndSaveMenu(bot, chatId, messageView.getMenuTitle(), keyboardFactory.createMainMenuButtons());
    }
}
