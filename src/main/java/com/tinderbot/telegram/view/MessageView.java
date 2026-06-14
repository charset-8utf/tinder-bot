package com.tinderbot.telegram.view;

import com.tinderbot.telegram.common.config.MenuOptionRegistry;
import com.tinderbot.telegram.common.util.ResourceConstants;
import com.tinderbot.telegram.core.BotResourceLoader;
import com.tinderbot.telegram.model.MenuOption;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageView {

    private static final String MENU_TITLE = "Выберите режим работы:";
    private static final String UNKNOWN_COMMAND = "Неизвестная команда";
    private static final String HELP_MESSAGE = "Я не понимаю... Немного подумаю и решу что делать дальше!";
    private static final String RESOURCE_TYPE_MESSAGE = "message";
    private static final String RESOURCE_TYPE_PROMPT = "prompt";

    private final BotResourceLoader resourceLoader;
    private final MenuOptionRegistry menuOptions;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, String> prompts = new HashMap<>();

    @PostConstruct
    void preloadResources() {
        List<String> messageKeys = List.of(
                ResourceConstants.MESSAGE_MAIN,
                ResourceConstants.MESSAGE_GPT,
                ResourceConstants.MESSAGE_DATE_LIST,
                ResourceConstants.MESSAGE_MESSAGE_INTRO,
                ResourceConstants.MESSAGE_PROFILE_INTRO,
                ResourceConstants.MESSAGE_OPENER_INTRO
        );
        messageKeys.forEach(key -> messages.put(key, resourceLoader.loadMessage(key)));

        List<String> promptKeys = List.of(
                ResourceConstants.PROMPT_GPT,
                ResourceConstants.PROMPT_DATE_GRANDE,
                ResourceConstants.PROMPT_DATE_ROBBIE,
                ResourceConstants.PROMPT_DATE_ZENDAYA,
                ResourceConstants.PROMPT_DATE_GOSLING,
                ResourceConstants.PROMPT_DATE_HARDY,
                ResourceConstants.PROMPT_MESSAGE_NEXT,
                ResourceConstants.PROMPT_MESSAGE_DATE,
                ResourceConstants.PROMPT_PROFILE,
                ResourceConstants.PROMPT_OPENER
        );
        promptKeys.forEach(key -> prompts.put(key, resourceLoader.loadPrompt(key)));
    }

    private String getRequiredResource(Map<String, String> cache, String key, String resourceType) {
        return cache.computeIfAbsent(key, missing -> {
            throw new IllegalStateException("Missing preloaded " + resourceType + " resource: " + missing);
        });
    }

    public String getMenuTitle() {
        return MENU_TITLE;
    }

    public String getResponseForCallback(String callback) {
        return menuOptions.findByCallback(callback)
                .map(MenuOption::getResponseText)
                .orElse(UNKNOWN_COMMAND);
    }

    public String getWelcomeText() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_MAIN, RESOURCE_TYPE_MESSAGE);
    }

    public String getHelpMessage() {
        return HELP_MESSAGE;
    }

    public String getGptMessage() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_GPT, RESOURCE_TYPE_MESSAGE);
    }

    public String getGptPrompt() {
        return getRequiredResource(prompts, ResourceConstants.PROMPT_GPT, RESOURCE_TYPE_PROMPT);
    }

    public String getDateListMessage() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_DATE_LIST, RESOURCE_TYPE_MESSAGE);
    }

    public String getMessageIntro() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_MESSAGE_INTRO, RESOURCE_TYPE_MESSAGE);
    }

    public String getMessagePrompt() {
        return getRequiredResource(prompts, ResourceConstants.PROMPT_MESSAGE_NEXT, RESOURCE_TYPE_PROMPT);
    }

    public String getMessageDatePrompt() {
        return getRequiredResource(prompts, ResourceConstants.PROMPT_MESSAGE_DATE, RESOURCE_TYPE_PROMPT);
    }

    public String getProfileIntro() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_PROFILE_INTRO, RESOURCE_TYPE_MESSAGE);
    }

    public String getProfilePrompt() {
        return getRequiredResource(prompts, ResourceConstants.PROMPT_PROFILE, RESOURCE_TYPE_PROMPT);
    }

    public String getOpenerIntro() {
        return getRequiredResource(messages, ResourceConstants.MESSAGE_OPENER_INTRO, RESOURCE_TYPE_MESSAGE);
    }

    public String getOpenerPrompt() {
        return getRequiredResource(prompts, ResourceConstants.PROMPT_OPENER, RESOURCE_TYPE_PROMPT);
    }

    public String loadPromptByKey(String key) {
        return prompts.computeIfAbsent(key, resourceLoader::loadPrompt);
    }
}
