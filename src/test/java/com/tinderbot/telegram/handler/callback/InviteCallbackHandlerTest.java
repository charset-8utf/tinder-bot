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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteCallbackHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IChatGPTService chatGPTService;
    @Mock private MessageView messageView;
    @Mock private MultiSessionTelegramBot bot;

    private InviteCallbackHandler handler;

    @BeforeEach
    void setUp() {
        MessageDialogService messageDialogService = DialogServiceTestFixtures.messageDialogService(
                sessionService, chatGPTService, messageView);
        handler = new InviteCallbackHandler(sessionService, messageDialogService);
    }

    @Test
    void supports_shouldReturnTrueForInvite() {
        assertThat(handler.supports(CallbackConstants.INVITE)).isTrue();
        assertThat(handler.supports("other")).isFalse();
    }

    @Test
    void execute_withEmptyHistory_shouldSendWarning() {
        when(sessionService.getMessageHistory(1L)).thenReturn(List.of());

        handler.execute(bot, 1L, CallbackConstants.INVITE);

        verify(bot).sendTextMessage("Сначала отправьте вашу переписку.");
        verifyNoInteractions(chatGPTService);
    }

    @Test
    void execute_withHistory_shouldGenerateInvite() {
        when(sessionService.getMessageHistory(1L)).thenReturn(List.of("msg1", "msg2"));
        when(messageView.getMessageDatePrompt()).thenReturn("date_prompt");
        when(chatGPTService.sendMessage(1L, "date_prompt", "msg1\nmsg2")).thenReturn("answer");
        Message confirmMsg = mock(Message.class);
        when(bot.sendTextMessage("✅ Ваше сообщение принято!")).thenReturn(confirmMsg);
        when(confirmMsg.getMessageId()).thenReturn(101);
        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT придумывает приглашение...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(102);

        handler.execute(bot, 1L, CallbackConstants.INVITE);

        verify(sessionService).addBotMessageId(1L, 101);
        verify(sessionService).addBotMessageId(1L, 102);
        verify(chatGPTService).sendMessage(1L, "date_prompt", "msg1\nmsg2");
        verify(bot).updateTextMessage(thinkingMsg, "answer");
    }
}
