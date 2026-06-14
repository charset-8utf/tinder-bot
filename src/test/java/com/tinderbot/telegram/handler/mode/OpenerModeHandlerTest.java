package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.service.questionnaire.OpenerQuestionnaireStrategy;
import com.tinderbot.telegram.testsupport.QuestionnaireTestFixtures;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenerModeHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private IChatGPTService chatGPTService;
    @Mock private MessageSender messageSender;
    @Mock private MultiSessionTelegramBot bot;

    private OpenerModeHandler handler;

    @BeforeEach
    void setUp() {
        var questionnaireService = QuestionnaireTestFixtures.questionnaireService(sessionService, messageView);
        var generationService = QuestionnaireTestFixtures.generationService(chatGPTService);
        handler = new OpenerModeHandler(
                sessionService, messageCleaner, messageSender, questionnaireService, generationService);
    }

    @Test
    void getMode_shouldReturnOpener() {
        assertThat(handler.getMode()).isEqualTo(DialogMode.OPENER);
    }

    @Test
    void onCommand_shouldResetAndSendWelcome() {
        when(messageView.getOpenerIntro()).thenReturn("opener intro");
        when(messageSender.sendAndSavePhoto(bot, 1L, ResourceConstants.IMAGE_OPENER)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveText(bot, 1L, "opener intro")).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveButtons(any(), anyLong(), anyString(), any(String[].class))).thenReturn(mock(Message.class));

        handler.onCommand(bot, 1L);

        verify(messageCleaner).deleteAllMessages(1L, bot);
        verify(sessionService).setOpenerStep(1L, 0);
        verify(sessionService).setOpenerTemp(eq(1L), any(UserInfo.class));
        verify(messageSender).sendAndSavePhoto(bot, 1L, ResourceConstants.IMAGE_OPENER);
        verify(messageSender).sendAndSaveText(bot, 1L, "opener intro");
        verify(messageSender, times(1)).sendAndSaveButtons(any(), anyLong(), anyString(), any(String[].class));
        verify(sessionService).setCurrentMode(1L, DialogMode.OPENER);
    }

    @Test
    void onMessage_shouldSaveAnswerAndProceed() {
        when(sessionService.getOpenerStep(1L)).thenReturn(0);
        UserInfo temp = new UserInfo();
        when(sessionService.getOpenerTemp(1L)).thenReturn(temp);

        handler.onMessage(bot, 1L, "Anna");

        verify(sessionService).setOpenerStep(1L, 1);
        verify(messageSender).sendAndSaveText(bot, 1L, new OpenerQuestionnaireStrategy(sessionService).questions()[1]);
    }

    @Test
    void onMessage_lastAnswer_shouldComplete() {
        when(sessionService.getOpenerStep(1L)).thenReturn(4);
        UserInfo temp = new UserInfo();
        when(sessionService.getOpenerTemp(1L)).thenReturn(temp);
        when(messageView.getOpenerPrompt()).thenReturn("opener prompt");
        when(chatGPTService.sendMessage(eq(1L), eq("opener prompt"), anyString())).thenReturn("opener message");

        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT придумывает первое сообщение...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(123);

        handler.onMessage(bot, 1L, "dating");

        verify(sessionService).setOpenerStep(1L, 0);
        verify(messageSender).sendAndSaveText(bot, 1L, "✅ Ваши ответы приняты!");
        verify(chatGPTService).sendMessage(eq(1L), eq("opener prompt"), anyString());
        verify(bot).sendTextMessage("ChatGPT придумывает первое сообщение...");
        verify(bot).updateTextMessage(thinkingMsg, "opener message");
    }
}
