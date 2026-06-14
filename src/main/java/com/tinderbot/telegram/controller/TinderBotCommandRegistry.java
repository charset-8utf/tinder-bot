package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.api.ModeHandler;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TinderBotCommandRegistry {

    private final Map<DialogMode, ModeHandler> byMode;
    private final Map<String, BiConsumer<MultiSessionTelegramBot, Long>> byCommand;

    public TinderBotCommandRegistry(List<ModeHandler> modeHandlers) {
        this.byMode = modeHandlers.stream()
                .collect(Collectors.toMap(ModeHandler::getMode, Function.identity()));
        this.byCommand = Map.of(
                "/start", (bot, chatId) -> byMode.get(DialogMode.MAIN).onCommand(bot, chatId),
                "/gpt", (bot, chatId) -> byMode.get(DialogMode.GPT).onCommand(bot, chatId),
                "/date", (bot, chatId) -> byMode.get(DialogMode.DATE).onCommand(bot, chatId),
                "/message", (bot, chatId) -> byMode.get(DialogMode.MESSAGE).onCommand(bot, chatId),
                "/profile", (bot, chatId) -> byMode.get(DialogMode.PROFILE).onCommand(bot, chatId),
                "/opener", (bot, chatId) -> byMode.get(DialogMode.OPENER).onCommand(bot, chatId)
        );
    }

    public Optional<BiConsumer<MultiSessionTelegramBot, Long>> findCommand(String command) {
        return Optional.ofNullable(byCommand.get(command));
    }

    public Optional<ModeHandler> findModeHandler(DialogMode mode) {
        return Optional.ofNullable(byMode.get(mode));
    }

    public ModeHandler requireModeHandler(DialogMode mode) {
        return byMode.get(mode);
    }
}
