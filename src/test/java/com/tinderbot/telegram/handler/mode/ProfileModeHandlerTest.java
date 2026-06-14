package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import com.tinderbot.telegram.service.questionnaire.ProfileQuestionnaireStrategy;
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
class ProfileModeHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private IChatGPTService chatGPTService;
    @Mock private MessageSender messageSender;
    @Mock private MultiSessionTelegramBot bot;

    private ProfileModeHandler handler;

    @BeforeEach
    void setUp() {
        var questionnaireService = QuestionnaireTestFixtures.questionnaireService(sessionService, messageView);
        var generationService = QuestionnaireTestFixtures.generationService(chatGPTService);
        handler = new ProfileModeHandler(
                sessionService, messageCleaner, messageSender, questionnaireService, generationService);
    }

    @Test
    void getMode_shouldReturnProfile() {
        assertThat(handler.getMode()).isEqualTo(DialogMode.PROFILE);
    }

    @Test
    void onCommand_shouldResetAndSendWelcome() {
        when(messageView.getProfileIntro()).thenReturn("intro");
        when(messageSender.sendAndSavePhoto(bot, 1L, ResourceConstants.IMAGE_PROFILE)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveHtmlText(bot, 1L, "intro")).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveButtons(any(), anyLong(), anyString(), any(String[].class))).thenReturn(mock(Message.class));

        handler.onCommand(bot, 1L);

        verify(messageCleaner).deleteAllMessages(1L, bot);
        verify(sessionService).setProfileStep(1L, 0);
        verify(sessionService).setProfileTemp(eq(1L), any(UserInfo.class));
        verify(messageSender).sendAndSavePhoto(bot, 1L, ResourceConstants.IMAGE_PROFILE);
        verify(messageSender).sendAndSaveHtmlText(bot, 1L, "intro");
        verify(messageSender, times(1)).sendAndSaveButtons(any(), anyLong(), anyString(), any(String[].class));
        verify(sessionService).setCurrentMode(1L, DialogMode.PROFILE);
    }

    @Test
    void onMessage_shouldSaveAnswerAndProceed() {
        when(sessionService.getProfileStep(1L)).thenReturn(0);
        UserInfo temp = new UserInfo();
        when(sessionService.getProfileTemp(1L)).thenReturn(temp);

        handler.onMessage(bot, 1L, "30");

        verify(sessionService).setProfileStep(1L, 1);
        verify(messageSender).sendAndSaveText(bot, 1L, new ProfileQuestionnaireStrategy(sessionService).questions()[1]);
    }

    @Test
    void onMessage_lastAnswer_shouldComplete() {
        when(sessionService.getProfileStep(1L)).thenReturn(4);
        UserInfo temp = new UserInfo();
        when(sessionService.getProfileTemp(1L)).thenReturn(temp);
        when(messageView.getProfilePrompt()).thenReturn("profile prompt");
        when(chatGPTService.sendMessage(eq(1L), eq("profile prompt"), anyString())).thenReturn("profile");

        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT создаёт ваш профиль...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(123);

        handler.onMessage(bot, 1L, "love");

        verify(sessionService).setProfileStep(1L, 0);
        verify(messageSender).sendAndSaveText(bot, 1L, "✅ Ваши ответы приняты!");
        verify(chatGPTService).sendMessage(1L, "profile prompt", temp.toString());
        verify(bot).sendTextMessage("ChatGPT создаёт ваш профиль...");
        verify(bot).updateTextMessage(thinkingMsg, "profile");
    }
}
