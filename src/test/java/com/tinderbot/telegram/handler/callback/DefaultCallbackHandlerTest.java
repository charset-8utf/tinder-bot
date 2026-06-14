package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.config.MenuOptionRegistry;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.MenuOption;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCallbackHandlerTest {

    @Mock
    private TelegramUiSessionStore telegramUi;
    @Mock
    private SessionModeStore sessionModes;
    @Mock
    private IMessageCleaner messageCleaner;
    @Mock
    private MessageView messageView;
    @Mock
    private KeyboardFactory keyboardFactory;
    @Mock
    private Map<MenuOption, DialogMode> optionToMode;
    @Mock
    private MultiSessionTelegramBot bot;

    private final MenuOptionRegistry menuOptions = new MenuOptionRegistry();
    private DefaultCallbackHandler handler;

    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        handler = new DefaultCallbackHandler(
                telegramUi, sessionModes, messageCleaner, messageView, keyboardFactory, optionToMode, menuOptions);
    }

    @Test
    void supports_shouldReturnFalseForStartButton() {
        assertFalse(handler.supports(MenuOption.START.getCallback()));
    }

    @Test
    void supports_shouldReturnFalseForGptButton() {
        assertFalse(handler.supports(MenuOption.GPT.getCallback()));
    }

    @Test
    void supports_shouldReturnFalseForStarButtons() {
        assertFalse(handler.supports("btn_star_gosling"));
    }

    @Test
    void supports_shouldReturnTrueForOtherButtons() {
        assertFalse(handler.supports("some_other_callback"));
    }

    @Test
    void execute_shouldDeleteMessagesAndSendResponse() {
        String callback = "btn_profile";
        when(messageView.getResponseForCallback(callback)).thenReturn("Profile chosen");
        when(keyboardFactory.createBackToMainMenuButton()).thenReturn(new String[]{"Back", "btn_start"});
        Message resultMsg = mock(Message.class);
        when(resultMsg.getMessageId()).thenReturn(301);
        when(bot.sendTextButtonsMessage(eq("Profile chosen"), any(String[].class))).thenReturn(resultMsg);

        handler.execute(bot, chatId, callback);

        verify(messageCleaner).deleteAllMessages(chatId, bot);
        verify(telegramUi).setCurrentMenuMessageId(chatId, 301);
        verify(telegramUi).setPhotoMessageId(chatId, null);
        verify(telegramUi).setWelcomeMessageId(chatId, null);
    }

    @Test
    void execute_shouldSetModeFromOptionToMode() {
        String callback = "btn_profile";
        when(messageView.getResponseForCallback(callback)).thenReturn("Profile chosen");
        when(keyboardFactory.createBackToMainMenuButton()).thenReturn(new String[]{"Back", "btn_start"});
        Message resultMsg = mock(Message.class);
        when(resultMsg.getMessageId()).thenReturn(301);
        when(bot.sendTextButtonsMessage(eq("Profile chosen"), any(String[].class))).thenReturn(resultMsg);
        when(optionToMode.getOrDefault(MenuOption.PROFILE, DialogMode.MAIN)).thenReturn(DialogMode.PROFILE);

        handler.execute(bot, chatId, callback);

        verify(sessionModes).setCurrentMode(chatId, DialogMode.PROFILE);
    }
}
