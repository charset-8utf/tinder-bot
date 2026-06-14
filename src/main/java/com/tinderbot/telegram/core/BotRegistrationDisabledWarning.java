package com.tinderbot.telegram.core;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(name = "telegram.bot.register", havingValue = "false")
public class BotRegistrationDisabledWarning {

    @PostConstruct
    void warn() {
        log.warn("""
                telegram.bot.register=false — бот НЕ подписан на long polling и не получает сообщения.
                Для работы в Telegram установите telegram.bot.register=true и корректный telegram.bot.token.""");
    }
}
