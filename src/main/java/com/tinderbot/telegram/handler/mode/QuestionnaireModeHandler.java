package com.tinderbot.telegram.handler.mode;

import com.tinderbot.telegram.api.IMessageCleaner;
import com.tinderbot.telegram.api.session.TelegramUiSessionStore;
import com.tinderbot.telegram.api.ModeHandler;
import com.tinderbot.telegram.common.util.MessageSender;
import com.tinderbot.telegram.core.MultiSessionTelegramBot;
import com.tinderbot.telegram.model.*;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireGenerationService;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@RequiredArgsConstructor
public abstract class QuestionnaireModeHandler implements ModeHandler {

    protected final TelegramUiSessionStore telegramUi;
    protected final IMessageCleaner messageCleaner;
    protected final MessageSender messageSender;
    protected final QuestionnaireService questionnaireService;
    protected final QuestionnaireGenerationService generationService;

    protected abstract QuestionnaireType getQuestionnaireType();

    @Override
    public void onCommand(MultiSessionTelegramBot bot, Long chatId) {
        messageCleaner.deleteAllMessages(chatId, bot);
        QuestionnaireProgress progress = questionnaireService.start(chatId, getQuestionnaireType());
        if (progress instanceof QuestionnaireProgress.Started started) {
            sendWelcomeScreen(bot, chatId, started);
            return;
        }
        throw new IllegalStateException("start() должен возвращать Started, получено: " + progress);
    }

    private void sendWelcomeScreen(MultiSessionTelegramBot bot, Long chatId, QuestionnaireProgress.Started started) {
        messageSender.sendAndSavePhoto(bot, chatId, started.photoKey());
        messageSender.sendAndSaveHtmlText(bot, chatId, started.intro());
        sendAndSaveBackButton(bot, chatId);
        messageSender.sendAndSaveText(bot, chatId, started.firstQuestion());
    }

    private void sendAndSaveBackButton(MultiSessionTelegramBot bot, Long chatId) {
        messageSender.sendAndSaveButtons(bot, chatId,
                "Чтобы прервать опрос, нажмите кнопку:",
                "Главное меню", MenuOption.START.getCallback()
        );
    }

    @Override
    public void onMessage(MultiSessionTelegramBot bot, Long chatId, String text) {
        if (bot.isMessageCommand()) {
            return;
        }
        if (text == null || text.isBlank()) {
            return;
        }
        try {
            QuestionnaireProgress progress = questionnaireService.submitAnswer(chatId, getQuestionnaireType(), text);
            handleProgress(bot, chatId, progress);
        } catch (IllegalArgumentException e) {
            log.debug("Пустой ответ от пользователя {}", chatId);
        }
    }

    private void handleProgress(MultiSessionTelegramBot bot, Long chatId, QuestionnaireProgress progress) {
        switch (progress) {
            case QuestionnaireProgress.NextQuestion(String question) ->
                    messageSender.sendAndSaveText(bot, chatId, question);
            case QuestionnaireProgress.Completed completed -> completeQuestionnaire(bot, chatId, completed);
            case QuestionnaireProgress.Started started ->
                    throw new IllegalStateException("submitAnswer() не возвращает Started, получено: " + started);
        }
    }

    private void completeQuestionnaire(MultiSessionTelegramBot bot, Long chatId, QuestionnaireProgress.Completed completed) {
        messageSender.sendAndSaveText(bot, chatId, "✅ Ваши ответы приняты!");

        Message thinkingMsg = bot.sendTextMessage(completed.thinkingMessage());
        telegramUi.addBotMessageId(chatId, thinkingMsg.getMessageId());

        TextGenerationResult result = generationService.generate(
                chatId, getQuestionnaireType(), completed.prompt(), completed.userData());
        if (result.failed()) {
            bot.updateTextMessage(thinkingMsg, "Извините, произошла ошибка.");
        } else {
            bot.updateTextMessage(thinkingMsg, result.text().orElse(""));
        }

        messageSender.sendAndSaveButtons(bot, chatId,
                "Вернуться в главное меню:",
                "Главное меню", MenuOption.START.getCallback()
        );
    }

    @Override
    public DialogMode getMode() {
        return questionnaireService.dialogMode(getQuestionnaireType());
    }
}
