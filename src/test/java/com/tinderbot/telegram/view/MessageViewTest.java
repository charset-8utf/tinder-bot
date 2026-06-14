package com.tinderbot.telegram.view;

import com.tinderbot.telegram.common.config.MenuOptionRegistry;
import com.tinderbot.telegram.core.BotResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageViewTest {

    private MessageView messageView;

    @BeforeEach
    void setUp() {
        messageView = new MessageView(new BotResourceLoader(), new MenuOptionRegistry());
        messageView.preloadResources();
    }

    @Test
    void getWelcomeText_shouldLoadMainMessage() {
        String text = messageView.getWelcomeText();
        assertThat(text).isNotEmpty();
    }

    @Test
    void getGptMessage_shouldLoadGptMessage() {
        String text = messageView.getGptMessage();
        assertThat(text).isNotEmpty();
    }

    @Test
    void getGptPrompt_shouldLoadGptPrompt() {
        String prompt = messageView.getGptPrompt();
        assertThat(prompt).isNotEmpty();
    }
}
