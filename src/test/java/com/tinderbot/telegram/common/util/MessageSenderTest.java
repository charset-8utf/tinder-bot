package com.tinderbot.telegram.common.util;

import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSenderTest {

    @Mock
    private TelegramUiSessionStore telegramUi;
    @Mock
    private MultiSessionTelegramBot bot;
    @InjectMocks
    private MessageSender messageSender;

    @Test
    void sendAndSavePhoto_shouldSavePhotoId() {
        Message photoMsg = mock(Message.class);
        when(bot.sendPhotoMessage("test.jpg")).thenReturn(photoMsg);
        when(photoMsg.getMessageId()).thenReturn(101);

        messageSender.sendAndSavePhoto(bot, 1L, "test.jpg");

        verify(telegramUi).setPhotoMessageId(1L, 101);
    }

    @Test
    void sendAndSaveText_shouldAddBotMessageId() {
        Message textMsg = mock(Message.class);
        when(bot.sendTextMessage("hello")).thenReturn(textMsg);
        when(textMsg.getMessageId()).thenReturn(102);

        messageSender.sendAndSaveText(bot, 1L, "hello");

        verify(telegramUi).addBotMessageId(1L, 102);
    }

    @Test
    void sendAndSaveButtons_shouldAddBotMessageId() {
        Message buttonsMsg = mock(Message.class);
        when(bot.sendTextButtonsMessage("text", "btn1", "cb1")).thenReturn(buttonsMsg);
        when(buttonsMsg.getMessageId()).thenReturn(103);

        messageSender.sendAndSaveButtons(bot, 1L, "text", "btn1", "cb1");

        verify(telegramUi).addBotMessageId(1L, 103);
    }

    @Test
    void sendAndSaveMenu_shouldSetCurrentMenuMessageId() {
        Message menuMsg = mock(Message.class);
        when(bot.sendTextButtonsMessage("menu", "btn1", "cb1")).thenReturn(menuMsg);
        when(menuMsg.getMessageId()).thenReturn(104);

        messageSender.sendAndSaveMenu(bot, 1L, "menu", "btn1", "cb1");

        verify(telegramUi).setCurrentMenuMessageId(1L, 104);
    }
}