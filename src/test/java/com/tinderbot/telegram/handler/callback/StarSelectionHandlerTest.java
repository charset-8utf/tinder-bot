package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.Star;
import com.tinderbot.telegram.common.config.StarRegistry;
import com.tinderbot.telegram.service.dialog.DateDialogService;
import com.tinderbot.telegram.testsupport.DialogServiceTestFixtures;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StarSelectionHandlerTest {

    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private IChatGPTService chatGPTService;
    @Mock private ISessionService sessionService;
    @Mock private MultiSessionTelegramBot bot;

    private StarSelectionHandler handler;

    @BeforeEach
    void setUp() {
        DateDialogService dateDialogService = DialogServiceTestFixtures.dateDialogService(
                sessionService, chatGPTService, messageView);
        handler = new StarSelectionHandler(messageCleaner, sessionService, dateDialogService);
    }

    @Test
    void supports_shouldReturnTrueForStarPrefix() {
        assertThat(handler.supports(CallbackConstants.STAR_PREFIX + "gosling")).isTrue();
        assertThat(handler.supports("other")).isFalse();
    }

    @Test
    void execute_withValidStar_shouldProceed() {
        String callback = CallbackConstants.STAR_PREFIX + "gosling";
        Star star = Star.GOSLING;

        Message photoMsg = mock(Message.class);
        when(bot.sendPhotoMessage(star.getPhotoKey())).thenReturn(photoMsg);
        when(photoMsg.getMessageId()).thenReturn(101);
        when(messageView.loadPromptByKey(star.getPromptKey())).thenReturn("prompt");

        Message instructionMsg = mock(Message.class);
        when(bot.sendTextMessage(anyString())).thenReturn(instructionMsg);
        when(instructionMsg.getMessageId()).thenReturn(102);

        Message backButtonsMsg = mock(Message.class);
        when(bot.sendTextButtonsMessage(anyString(), any(String[].class))).thenReturn(backButtonsMsg);
        when(backButtonsMsg.getMessageId()).thenReturn(103);

        handler.execute(bot, 1L, callback);

        verify(messageCleaner).deleteAllMessages(1L, bot);
        verify(sessionService).setPhotoMessageId(1L, 101);
        verify(chatGPTService).setPrompt(1L, "prompt");
        verify(sessionService).resetDateMessageCount(1L);
        verify(sessionService).clearChatGptHistory(1L);
        verify(sessionService).addBotMessageId(1L, 102);
        verify(sessionService).addBotMessageId(1L, 103);
        verify(sessionService).setCurrentStarKey(1L, "gosling");
        verify(sessionService).setCurrentMode(1L, DialogMode.DATE);
    }

    @Test
    void execute_withInvalidStar_shouldSendErrorMessage() {
        String callback = CallbackConstants.STAR_PREFIX + "unknown";
        handler.execute(bot, 1L, callback);
        verify(bot).sendTextMessage("Неизвестная звезда");
    }
}
