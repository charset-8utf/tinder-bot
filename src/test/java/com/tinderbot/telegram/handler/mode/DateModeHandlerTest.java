package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.common.config.StarRegistry;
import com.tinderbot.telegram.service.dialog.DateDialogService;
import com.tinderbot.telegram.testsupport.DialogServiceTestFixtures;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateModeHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private IChatGPTService chatGPTService;
    @Mock private KeyboardFactory keyboardFactory;
    @Mock private MessageSender messageSender;
    @Mock private MultiSessionTelegramBot bot;

    private DateModeHandler handler;
    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        DateDialogService dateDialogService = DialogServiceTestFixtures.dateDialogService(
                sessionService, chatGPTService, messageView);
        handler = new DateModeHandler(
                messageCleaner, messageView, keyboardFactory, messageSender, sessionService, dateDialogService);
    }

    @Test
    void getMode_shouldReturnDate() {
        assertEquals(DialogMode.DATE, handler.getMode());
    }

    @Test
    void onCommand_shouldSendWelcomeScreen() {
        String dateList = "date list";
        String[] starButtons = {"star1", "cb1", "star2", "cb2"};

        when(messageView.getDateListMessage()).thenReturn(dateList);
        when(keyboardFactory.createStarButtons()).thenReturn(starButtons);
        when(messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_DATE)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveText(bot, chatId, dateList)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveButtons(bot, chatId, "Список доступных профилей:", starButtons)).thenReturn(mock(Message.class));
        when(messageSender.sendAndSaveButtons(bot, chatId, "...или можно вернуться в главное меню.", "Главное меню", "btn_start"))
                .thenReturn(mock(Message.class));

        handler.onCommand(bot, chatId);

        verify(messageCleaner).deleteAllMessages(chatId, bot);
        verify(sessionService).setCurrentStarKey(chatId, null);
        verify(messageSender).sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_DATE);
        verify(messageSender).sendAndSaveText(bot, chatId, dateList);
        verify(messageSender).sendAndSaveButtons(bot, chatId, "Список доступных профилей:", starButtons);
        verify(messageSender).sendAndSaveButtons(bot, chatId, "...или можно вернуться в главное меню.", "Главное меню", "btn_start");
        verify(sessionService).setCurrentMode(chatId, DialogMode.DATE);
    }

    @Test
    void onMessage_shouldSendPersonalizedThinkingAndCallAddMessage() {
        String starKey = "gosling";
        when(sessionService.getCurrentStarKey(chatId)).thenReturn(starKey);
        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("Райан Гослинг печатает...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(201);
        when(chatGPTService.addMessage(chatId, "hello")).thenReturn("answer");

        handler.onMessage(bot, chatId, "hello");

        verify(sessionService).addBotMessageId(chatId, 201);
        verify(chatGPTService).addMessage(chatId, "hello");
        verify(bot).updateTextMessage(thinkingMsg, "answer");
    }

    @Test
    void onMessage_withoutStarKey_shouldSendDefaultThinking() {
        when(sessionService.getCurrentStarKey(chatId)).thenReturn(null);
        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("Собеседник печатает...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(202);
        when(chatGPTService.addMessage(chatId, "hello")).thenReturn("answer");

        handler.onMessage(bot, chatId, "hello");

        verify(sessionService).addBotMessageId(chatId, 202);
        verify(chatGPTService).addMessage(chatId, "hello");
        verify(bot).updateTextMessage(thinkingMsg, "answer");
    }

    @Test
    void onMessage_whenCommand_shouldDoNothing() {
        when(bot.isMessageCommand()).thenReturn(true);
        handler.onMessage(bot, chatId, "/command");
        verifyNoInteractions(sessionService, chatGPTService);
        verify(bot, never()).sendTextMessage(anyString());
    }
}
