package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.dialog.GptDialogService;
import com.tinderbot.telegram.testsupport.DialogServiceTestFixtures;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GptModeHandlerTest {

    @Mock private ISessionService sessionService;
    @Mock private IMessageCleaner messageCleaner;
    @Mock private MessageView messageView;
    @Mock private IChatGPTService chatGPTService;
    @Mock private KeyboardFactory keyboardFactory;
    @Mock private MessageSender messageSender;
    @Mock private MultiSessionTelegramBot bot;

    private GptModeHandler handler;
    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        GptDialogService gptDialogService = new GptDialogService(
                sessionService, chatGPTService, messageView, DialogServiceTestFixtures.generationResults());
        handler = new GptModeHandler(
                messageCleaner, messageView, keyboardFactory, messageSender, sessionService, gptDialogService);
    }

    @Test
    void getMode_shouldReturnGpt() {
        assertEquals(DialogMode.GPT, handler.getMode());
    }

    @Test
    void onCommand_shouldCleanChatAndSendPhotoAndMenu() {
        Message photoMsg = mock(Message.class);
        when(messageSender.sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_GPT)).thenReturn(photoMsg);

        Message menuMsg = mock(Message.class);
        when(messageView.getGptMessage()).thenReturn("GPT message");
        String[] backButton = {"Главное меню", "btn_start"};
        when(keyboardFactory.createBackToMainMenuButton()).thenReturn(backButton);
        when(messageSender.sendAndSaveHtmlMenu(bot, chatId, "GPT message", backButton)).thenReturn(menuMsg);

        handler.onCommand(bot, chatId);

        verify(messageCleaner).deleteAllMessages(chatId, bot);
        verify(messageSender).sendAndSavePhoto(bot, chatId, ResourceConstants.IMAGE_GPT);
        verify(messageSender).sendAndSaveHtmlMenu(bot, chatId, "GPT message", backButton);
        verify(sessionService).setCurrentMode(chatId, DialogMode.GPT);
    }

    @Test
    void onMessage_whenNotCommand_shouldSendThinkingAndUpdateWithAnswer() {
        when(bot.isMessageCommand()).thenReturn(false);

        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT думает...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(301);
        when(messageView.getGptPrompt()).thenReturn("test prompt");
        when(chatGPTService.sendMessageAsync(chatId, "test prompt", "hello"))
                .thenReturn(CompletableFuture.completedFuture("answer"));

        handler.onMessage(bot, chatId, "hello");

        verify(sessionService).addBotMessageId(chatId, 301);
        verify(bot, timeout(1000)).updateTextMessage(thinkingMsg, "answer");
    }

    @Test
    void onMessage_whenCommand_shouldDoNothing() {
        when(bot.isMessageCommand()).thenReturn(true);

        handler.onMessage(bot, chatId, "/start");

        verify(bot, never()).sendTextMessage(anyString());
        verify(sessionService, never()).addBotMessageId(anyLong(), anyInt());
    }

    @Test
    void onMessage_whenAsyncFails_shouldSendErrorMessage() {
        when(bot.isMessageCommand()).thenReturn(false);

        Message thinkingMsg = mock(Message.class);
        when(bot.sendTextMessage("ChatGPT думает...")).thenReturn(thinkingMsg);
        when(thinkingMsg.getMessageId()).thenReturn(301);
        when(messageView.getGptPrompt()).thenReturn("test prompt");
        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("API error"));
        when(chatGPTService.sendMessageAsync(chatId, "test prompt", "hello")).thenReturn(failedFuture);

        handler.onMessage(bot, chatId, "hello");

        verify(sessionService).addBotMessageId(chatId, 301);
        verify(bot, timeout(1000)).updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
    }
}
