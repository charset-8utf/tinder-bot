package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.api.CallbackHandler;
import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.SessionModeStore;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.common.config.MenuOptionRegistry;
import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.MenuOption;
import com.tinderbot.telegram.view.KeyboardFactory;
import com.tinderbot.telegram.view.MessageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultCallbackHandler implements CallbackHandler {
    private static final Set<MenuOption> EXCLUDED_OPTIONS = EnumSet.of(
            MenuOption.START,
            MenuOption.GPT,
            MenuOption.DATE,
            MenuOption.MESSAGE,
            MenuOption.PROFILE,
            MenuOption.OPENER
    );
    private static final Predicate<String> IS_INTERNAL_CALLBACK = callback ->
            callback.startsWith(CallbackConstants.STAR_PREFIX)
                    || CallbackConstants.NEXT_MESSAGE.equals(callback)
                    || CallbackConstants.INVITE.equals(callback);

    private final TelegramUiSessionStore telegramUi;
    private final SessionModeStore sessionModes;
    private final IMessageCleaner messageCleaner;
    private final MessageView messageView;
    private final KeyboardFactory keyboardFactory;
    private final Map<MenuOption, DialogMode> optionToMode;
    private final MenuOptionRegistry menuOptions;

    @Override
    public boolean supports(String callback) {
        return Predicate.not(String::isBlank)
                .and(Predicate.not(IS_INTERNAL_CALLBACK))
                .test(callback == null ? "" : callback)
                && menuOptions.findByCallback(callback)
                .map(option -> !EXCLUDED_OPTIONS.contains(option))
                .orElse(false);
    }

    @Override
    public void execute(MultiSessionTelegramBot bot, Long chatId, String callback) {
        messageCleaner.deleteAllMessages(chatId, bot);
        String response = messageView.getResponseForCallback(callback);
        Message resultMsg = bot.sendTextButtonsMessage(
                response,
                keyboardFactory.createBackToMainMenuButton()
        );
        telegramUi.setCurrentMenuMessageId(chatId, resultMsg.getMessageId());
        telegramUi.setPhotoMessageId(chatId, null);
        telegramUi.setWelcomeMessageId(chatId, null);

        menuOptions.findByCallback(callback)
                .map(option -> optionToMode.getOrDefault(option, DialogMode.MAIN))
                .ifPresent(mode -> sessionModes.setCurrentMode(chatId, mode));
    }
}