package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.api.ModeHandler;
import com.tinderbot.telegram.common.config.TelegramBotProperties;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.core.BotResourceLoader;
import com.tinderbot.telegram.core.TelegramCommandNormalizer;
import com.tinderbot.telegram.core.TelegramTextTruncator;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.auth.TelegramUserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TinderBotControllerTest {

    @Mock private ModeHandler mainHandler;
    @Mock private ModeHandler gptHandler;
    @Mock private ModeHandler dateHandler;
    @Mock private ModeHandler messageHandler;
    @Mock private ModeHandler profileHandler;
    @Mock private ModeHandler openerHandler;

    @Mock private CallbackHandler startCallback;
    @Mock private CallbackHandler gptCallback;
    @Mock private CallbackHandler dateCallback;
    @Mock private CallbackHandler messageCallback;
    @Mock private CallbackHandler profileCallback;
    @Mock private CallbackHandler openerCallback;
    @Mock private CallbackHandler nextMessageCallback;
    @Mock private CallbackHandler inviteCallback;
    @Mock private CallbackHandler starSelectionHandler;

    @Mock private ISessionService sessionService;
    @Mock private TelegramUserRegistrationService userRegistrationService;
    @Mock private BotResourceLoader resourceLoader;
    @Mock private TelegramTextTruncator textTruncator;
    @Mock private TelegramCommandNormalizer commandNormalizer;
    @Mock private Update update;

    private TinderBotController controller;

    @BeforeEach
    void setUp() {
        when(mainHandler.getMode()).thenReturn(DialogMode.MAIN);
        when(gptHandler.getMode()).thenReturn(DialogMode.GPT);
        when(dateHandler.getMode()).thenReturn(DialogMode.DATE);
        when(messageHandler.getMode()).thenReturn(DialogMode.MESSAGE);
        when(profileHandler.getMode()).thenReturn(DialogMode.PROFILE);
        when(openerHandler.getMode()).thenReturn(DialogMode.OPENER);

        List<ModeHandler> modeHandlers = List.of(
                mainHandler, gptHandler, dateHandler, messageHandler, profileHandler, openerHandler
        );

        List<CallbackHandler> callbackHandlers = List.of(
                startCallback, gptCallback, dateCallback, messageCallback, profileCallback, openerCallback,
                nextMessageCallback, inviteCallback, starSelectionHandler
        );

        controller = spy(new TinderBotController(
                new TelegramBotProperties("testBot", "token", true),
                resourceLoader,
                textTruncator,
                commandNormalizer,
                sessionService,
                new TinderBotCommandRegistry(modeHandlers),
                callbackHandlers,
                userRegistrationService));
        doReturn(123L).when(controller).getCurrentChatId();
        doReturn(Optional.empty()).when(controller).getCurrentTelegramUsername();
        when(userRegistrationService.ensureRegistered(eq(123L), any()))
                .thenReturn(new TelegramUserRegistrationService.RegistrationResult(false, "user_123", null));
        when(commandNormalizer.normalize(anyString())).thenAnswer(inv -> {
            String raw = inv.getArgument(0);
            if (raw == null || raw.isBlank()) {
                return "";
            }
            String first = raw.trim().split("\\s+", 2)[0];
            int at = first.indexOf('@');
            if (at > 0) {
                first = first.substring(0, at);
            }
            return first;
        });

        Message dummyMessage = mock(Message.class);
        doReturn(dummyMessage).when(controller).sendTextMessage(anyString());
        doReturn(dummyMessage).when(controller).sendTextButtonsMessage(anyString(), any(String[].class));
        doReturn(dummyMessage).when(controller).sendPhotoMessage(anyString());
    }

    @Test
    void normalizeCommand_stripsAtSuffixAndPayload() {
        TelegramCommandNormalizer normalizer = new TelegramCommandNormalizer();
        assertEquals("/start", normalizer.normalize("/start@MyBot payload"));
        assertEquals("/gpt", normalizer.normalize("  /gpt"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/start", "/start@testBot", "/start ref123"})
    void onUpdateEventReceived_withStartVariants_shouldCallMainHandlerOnCommand(String messageText) {
        doReturn(messageText).when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(mainHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withGptCommand_shouldCallGptHandlerOnCommand() {
        doReturn("/gpt").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(gptHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withDateCommand_shouldCallDateHandlerOnCommand() {
        doReturn("/date").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(dateHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withMessageCommand_shouldCallMessageHandlerOnCommand() {
        doReturn("/message").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(messageHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withProfileCommand_shouldCallProfileHandlerOnCommand() {
        doReturn("/profile").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(profileHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withOpenerCommand_shouldCallOpenerHandlerOnCommand() {
        doReturn("/opener").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(openerHandler).onCommand(any(MultiSessionTelegramBot.class), eq(123L));
    }

    @Test
    void onUpdateEventReceived_withUnknownCommand_shouldSendUnknownMessage() {
        doReturn("/unknown").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        controller.onUpdateEventReceived(update);

        verify(controller).sendTextMessage("Неизвестная команда");
    }

    @Test
    void onUpdateEventReceived_withStartCallback_shouldProcessStartCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_start").when(controller).getCallbackQueryButtonKey();

        when(startCallback.supports("btn_start")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(startCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_start"));
    }

    @Test
    void onUpdateEventReceived_withGptCallback_shouldProcessGptCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_gpt").when(controller).getCallbackQueryButtonKey();

        when(gptCallback.supports("btn_gpt")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(gptCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_gpt"));
    }

    @Test
    void onUpdateEventReceived_withDateCallback_shouldProcessDateCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_date").when(controller).getCallbackQueryButtonKey();

        when(dateCallback.supports("btn_date")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(dateCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_date"));
    }

    @Test
    void onUpdateEventReceived_withMessageCallback_shouldProcessMessageCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_message").when(controller).getCallbackQueryButtonKey();

        when(messageCallback.supports("btn_message")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(messageCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_message"));
    }

    @Test
    void onUpdateEventReceived_withProfileCallback_shouldProcessProfileCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_profile").when(controller).getCallbackQueryButtonKey();

        when(profileCallback.supports("btn_profile")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(profileCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_profile"));
    }

    @Test
    void onUpdateEventReceived_withOpenerCallback_shouldProcessOpenerCallback() {
        doReturn("").when(controller).getMessageText();
        doReturn("btn_opener").when(controller).getCallbackQueryButtonKey();

        when(openerCallback.supports("btn_opener")).thenReturn(true);

        controller.onUpdateEventReceived(update);

        verify(openerCallback).execute(any(MultiSessionTelegramBot.class), eq(123L), eq("btn_opener"));
    }

    @Test
    void onUpdateEventReceived_withUnsupportedCallback_shouldNotExecuteAny() {
        doReturn("").when(controller).getMessageText();
        doReturn("unknown").when(controller).getCallbackQueryButtonKey();

        when(startCallback.supports("unknown")).thenReturn(false);
        when(gptCallback.supports("unknown")).thenReturn(false);
        when(dateCallback.supports("unknown")).thenReturn(false);
        when(messageCallback.supports("unknown")).thenReturn(false);
        when(profileCallback.supports("unknown")).thenReturn(false);
        when(openerCallback.supports("unknown")).thenReturn(false);
        when(nextMessageCallback.supports("unknown")).thenReturn(false);
        when(inviteCallback.supports("unknown")).thenReturn(false);
        when(starSelectionHandler.supports("unknown")).thenReturn(false);

        controller.onUpdateEventReceived(update);

        verify(startCallback, never()).execute(any(), anyLong(), anyString());
        verify(gptCallback, never()).execute(any(), anyLong(), anyString());
        verify(dateCallback, never()).execute(any(), anyLong(), anyString());
        verify(messageCallback, never()).execute(any(), anyLong(), anyString());
        verify(profileCallback, never()).execute(any(), anyLong(), anyString());
        verify(openerCallback, never()).execute(any(), anyLong(), anyString());
        verify(nextMessageCallback, never()).execute(any(), anyLong(), anyString());
        verify(inviteCallback, never()).execute(any(), anyLong(), anyString());
        verify(starSelectionHandler, never()).execute(any(), anyLong(), anyString());
    }

    @Test
    void onUpdateEventReceived_withTextInGptMode_shouldCallGptHandlerOnMessage() {
        doReturn("hello").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        when(sessionService.getCurrentMode(123L)).thenReturn(DialogMode.GPT);

        controller.onUpdateEventReceived(update);

        verify(gptHandler).onMessage(any(MultiSessionTelegramBot.class), eq(123L), eq("hello"));
    }

    @Test
    void onUpdateEventReceived_withTextInMainMode_shouldCallMainHandlerOnMessage() {
        doReturn("hello").when(controller).getMessageText();
        doReturn("").when(controller).getCallbackQueryButtonKey();

        when(sessionService.getCurrentMode(123L)).thenReturn(DialogMode.MAIN);

        controller.onUpdateEventReceived(update);

        verify(mainHandler).onMessage(any(MultiSessionTelegramBot.class), eq(123L), eq("hello"));
    }

    @Test
    void onUpdateEventReceived_withNullChatId_shouldLogWarning() {
        doReturn(null).when(controller).getCurrentChatId();

        controller.onUpdateEventReceived(update);

        verify(controller, never()).getMessageText();
        verify(controller, never()).getCallbackQueryButtonKey();
    }
}