package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.handler.mode.GptModeHandler;
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
class GptCallbackHandlerTest {

    @Mock
    private GptModeHandler gptModeHandler;
    @Mock
    private MultiSessionTelegramBot bot;

    @InjectMocks
    private GptCallbackHandler handler;

    @Test
    void supports_shouldReturnTrueForGptCallback() {
        assertTrue(handler.supports(MenuOption.GPT.getCallback()));
    }

    @Test
    void supports_shouldReturnFalseForOtherCallbacks() {
        assertFalse(handler.supports("other"));
        assertFalse(handler.supports("btn_start"));
    }

    @Test
    void execute_shouldCallGptModeHandlerOnCommand() {
        long chatId = 123L;

        handler.execute(bot, chatId, MenuOption.GPT.getCallback());

        verify(gptModeHandler).onCommand(bot, chatId);
    }
}