package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.core.BotResourceLoader;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.core.TelegramCommandNormalizer;
import com.tinderbot.telegram.core.TelegramTextTruncator;
import com.tinderbot.telegram.common.config.TelegramBotProperties;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.auth.TelegramUserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TinderBotController extends MultiSessionTelegramBot {

    private final SessionModeStore sessionModes;
    private final TinderBotCommandRegistry commandRegistry;
    private final List<CallbackHandler> callbackHandlers;
    private final TelegramCommandNormalizer commandNormalizer;
    private final TelegramUserRegistrationService userRegistrationService;

    public TinderBotController(
            TelegramBotProperties botProperties,
            BotResourceLoader resourceLoader,
            TelegramTextTruncator textTruncator,
            TelegramCommandNormalizer commandNormalizer,
            SessionModeStore sessionModes,
            TinderBotCommandRegistry commandRegistry,
            List<CallbackHandler> callbackHandlers,
            TelegramUserRegistrationService userRegistrationService) {
        super(botProperties.name(), botProperties.token(), resourceLoader, textTruncator);
        this.commandNormalizer = commandNormalizer;
        this.sessionModes = sessionModes;
        this.commandRegistry = commandRegistry;
        this.callbackHandlers = callbackHandlers;
        this.userRegistrationService = userRegistrationService;
        log.info("Контроллер бота '{}' инициализирован", botProperties.name());
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        try {
            Optional.ofNullable(getCurrentChatId())
                    .ifPresentOrElse(this::processCurrentUpdate, () -> log.warn("Не удалось определить chatId"));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TelegramApiException te) {
                log.error("Ошибка Telegram API: {}", te.getMessage(), te);
                notifyUser("Не удалось связаться с Telegram. Попробуйте позже.");
            } else {
                log.error("Необработанная ошибка в контроллере", e);
                notifyUser("Произошла внутренняя ошибка. Попробуйте позже.");
            }
        }
    }

    private void processCurrentUpdate(Long chatId) {
        registerTelegramUserIfNeeded(chatId);

        Optional.of(getCallbackQueryButtonKey())
                .filter(callback -> !callback.isBlank())
                .ifPresentOrElse(
                        callback -> processCallback(chatId, callback),
                        () -> processText(chatId, getMessageText())
                );
    }

    private void registerTelegramUserIfNeeded(Long chatId) {
        userRegistrationService.ensureRegistered(chatId, getCurrentTelegramUsername());
    }

    private void processCallback(Long chatId, String callback) {
        callbackHandlers.stream()
                .filter(handler -> handler.supports(callback))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.execute(this, chatId, callback),
                        () -> log.warn("Необработанный callback: {}", callback)
                );
    }

    private void processText(Long chatId, String text) {
        Optional.ofNullable(text)
                .filter(t -> !t.isBlank())
                .ifPresent(t -> {
                    if (t.startsWith("/")) {
                        processCommand(chatId, commandNormalizer.normalize(t));
                        return;
                    }
                    processUserMessage(chatId, t);
                });
    }

    private void processCommand(Long chatId, String command) {
        commandRegistry.findCommand(command)
                .ifPresentOrElse(
                        handler -> handler.accept(this, chatId),
                        () -> sendTextMessage("Неизвестная команда")
                );
    }

    private void processUserMessage(Long chatId, String text) {
        DialogMode mode = sessionModes.getCurrentMode(chatId);
        commandRegistry.findModeHandler(mode)
                .ifPresentOrElse(
                        handler -> handler.onMessage(this, chatId, text),
                        () -> {
                            log.warn("Нет обработчика для режима {}", mode);
                            commandRegistry.requireModeHandler(DialogMode.MAIN).onCommand(this, chatId);
                        }
                );
    }

    private void notifyUser(String text) {
        Optional.ofNullable(getCurrentChatId())
                .ifPresent(chatId -> sendTextMessage(text));
    }
}
