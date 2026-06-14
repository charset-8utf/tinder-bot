package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.telegram.MainMenuService;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainModeHandlerTest {

    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private KeyboardFactory keyboardFactory;
    @Mock private MessageSender messageSender;
    @Mock private MainMenuService mainMenuService;
    @Mock private MultiSessionTelegramBot bot;

    private MainModeHandler handler;
    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        handler = new MainModeHandler(messageCleaner, messageView, keyboardFactory, messageSender, mainMenuService);
    }

    @Test
    void getMode_shouldReturnMain() {
        assertEquals(DialogMode.MAIN, handler.getMode());
    }

    @Test
    void onCommand_shouldCleanChatAndSendMainMenu() {
        String welcomeText = "Welcome text";
        String menuTitle = "Menu title";
        String[] mainMenuButtons = {"btn1", "btn2"};

        when(messageView.getWelcomeText()).thenReturn(welcomeText);
        when(messageView.getMenuTitle()).thenReturn(menuTitle);
        when(keyboardFactory.createMainMenuButtons()).thenReturn(mainMenuButtons);
        when(messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_MAIN)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveHtmlText(bot, chatId, welcomeText)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveMenu(bot, chatId, menuTitle, mainMenuButtons)).thenReturn(mock(Message.class));

        handler.onCommand(bot, chatId);

        verify(messageCleaner).deleteAllMessages(chatId, bot);
        verify(mainMenuService).openMainMenu(chatId);
        verify(messageSender).sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_MAIN);
        verify(messageSender).sendAndSaveHtmlText(bot, chatId, welcomeText);
        verify(messageSender).sendAndSaveMenu(bot, chatId, menuTitle, mainMenuButtons);
    }

    @Test
    void onMessage_shouldDeleteCurrentMenuAndSendHelpAndMenu() {
        String helpMessage = "Help";
        String menuTitle = "Menu title";
        String[] mainMenuButtons = {"btn1", "btn2"};

        when(messageView.getHelpMessage()).thenReturn(helpMessage);
        when(messageView.getMenuTitle()).thenReturn(menuTitle);
        when(keyboardFactory.createMainMenuButtons()).thenReturn(mainMenuButtons);
        when(messageSender.sendAndSaveMenu(bot, chatId, menuTitle, mainMenuButtons)).thenReturn(mock(Message.class));

        handler.onMessage(bot, chatId, "some text");

        verify(messageCleaner).deleteCurrentMenu(chatId, bot);
        verify(bot).sendTextMessage(helpMessage);
        verify(messageSender).sendAndSaveMenu(bot, chatId, menuTitle, mainMenuButtons);
    }
}
