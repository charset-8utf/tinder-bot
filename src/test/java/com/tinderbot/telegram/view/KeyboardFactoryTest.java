package com.tinderbot.telegram.view;

import com.tinderbot.telegram.common.config.MenuOptionRegistry;
import com.tinderbot.telegram.model.MenuOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyboardFactoryTest {

    private KeyboardFactory factory;
    private final MenuOptionRegistry menuOptions = new MenuOptionRegistry();

    @BeforeEach
    void setUp() {
        factory = new KeyboardFactory();
    }

    @Test
    void createMainMenuButtons_shouldReturnCorrectPairs() {
        String[] buttons = factory.createMainMenuButtons();
        assertThat(buttons).isNotEmpty();
        assertThat(buttons.length % 2).isZero();

        for (int i = 1; i < buttons.length; i += 2) {
            assertThat(menuOptions.findByCallback(buttons[i])).isPresent();
        }
    }

    @Test
    void createBackToMainMenuButton_shouldReturnStartCallback() {
        String[] button = factory.createBackToMainMenuButton();
        assertThat(button).containsExactly("Главное меню бота", MenuOption.START.getCallback());
    }

    @Test
    void createStarButtons_shouldReturnCorrectPairs() {
        String[] buttons = factory.createStarButtons();
        assertThat(buttons).hasSize(10);
        for (int i = 1; i < buttons.length; i += 2) {
            assertThat(buttons[i]).startsWith("btn_star_");
        }
    }
}