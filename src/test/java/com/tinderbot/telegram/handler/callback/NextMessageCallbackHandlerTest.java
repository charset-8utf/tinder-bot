package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.service.dialog.MessageDialogService;
import com.tinderbot.telegram.testsupport.DialogServiceTestFixtures;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NextMessageCallbackHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IChatGPTService chatGPTService;
    @Mock private MessageView messageView;
    @Mock private MultiSessionTelegramBot bot;

    private NextMessageCallbackHandler handler;
    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        MessageDialogService messageDialogService = DialogServiceTestFixtures.messageDialogService(
                sessionService, chatGPTService, messageView);
        handler = new NextMessageCallbackHandler(sessionService, messageDialogService);
    }

    @Test
    void supports_shouldReturnTrueForNextMessageCallback() {
        assertTrue(handler.supports(CallbackConstants.NEXT_MESSAGE));
    }

    @Test
    void supports_shouldReturnFalseForOtherCallbacks() {
        assertFalse(handler.supports("other"));
        assertFalse(handler.supports(CallbackConstants.INVITE));
    }

    @Test
    void execute_withEmptyHistory_shouldSendWarning() {
        when(sessionService.getMessageHistory(chatId)).thenReturn(List.of());

        handler.execute(bot, chatId, CallbackConstants.NEXT_MESSAGE);

        verify(bot).sendTextMessage("Сначала отправьте вашу переписку.");
        verifyNoInteractions(chatGPTService);
    }

    @Test
    void execute_withHistory_shouldGenerateNextMessage() {
        List<String> history = List.of("msg1", "msg2");
        when(sessionService.getMessageHistory(chatId)).thenReturn(history);
        when(messageView.getMessagePrompt()).thenReturn("next_prompt");
        when(chatGPTService.sendMessage(chatId, "next_prompt", "msg1\nmsg2")).thenReturn("next answer");

        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT думает над следующим сообщением...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(301);

        handler.execute(bot, chatId, CallbackConstants.NEXT_MESSAGE);

        verify(sessionService).addBotMessageId(chatId, 301);
        verify(chatGPTService).sendMessage(chatId, "next_prompt", "msg1\nmsg2");
        verify(bot).updateTextMessage(thinkingMsg, "next answer");
    }
}
