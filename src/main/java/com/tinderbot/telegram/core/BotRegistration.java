package com.tinderbot.telegram.core;

import com.tinderbot.telegram.common.config.TelegramBotProperties;
import com.tinderbot.telegram.controller.TinderBotController;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.register", havingValue = "true", matchIfMissing = true)
public class BotRegistration {
    private final TinderBotController tinderBotController;
    private final TelegramBotProperties botProperties;

    @PostConstruct
    public void registerBot() {
        String token = botProperties.token();
        if (!StringUtils.hasText(token) || !token.contains(":")) {
            throw new IllegalStateException(
                    "TELEGRAM_BOT_TOKEN не задан или неверный формат. "
                            + "Возьмите токен вида 123456789:AAH... в @BotFather и пропишите в .env (переменная TELEGRAM_BOT_TOKEN), затем перезапустите: docker compose up -d --build app");
        }
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(tinderBotController);
            log.info("Бот '{}' успешно зарегистрирован!", botProperties.name());
        } catch (TelegramApiException e) {
            log.error(
                    "Ошибка регистрации бота в Telegram (long polling не запущен). "
                            + "Проверьте TELEGRAM_BOT_TOKEN и TELEGRAM_BOT_NAME. "
                            + "HTTP 404 от API часто означает неверный токен.",
                    e);
        }
    }
}