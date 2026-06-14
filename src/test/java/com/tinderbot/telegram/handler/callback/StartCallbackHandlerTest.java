package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.handler.mode.MainModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartCallbackHandlerTest {

    @Mock
    private MainModeHandler mainModeHandler;
    @Mock
    private MultiSessionTelegramBot bot;

    @InjectMocks
    private StartCallbackHandler handler;

    @Test
    void supports_shouldReturnTrueForStartCallback() {
        assertTrue(handler.supports(MenuOption.START.getCallback()));
    }

    @Test
    void supports_shouldReturnFalseForOtherCallbacks() {
        assertFalse(handler.supports("other"));
        assertFalse(handler.supports("btn_gpt"));
    }

    @Test
    void execute_shouldCallMainModeHandlerOnCommand() {
        long chatId = 123L;

        handler.execute(bot, chatId, MenuOption.START.getCallback());

        verify(mainModeHandler).onCommand(bot, chatId);
    }
}