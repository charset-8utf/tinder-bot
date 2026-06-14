package com.tinderbot.telegram.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramTextTruncatorTest {

    private TelegramTextTruncator truncator;

    @BeforeEach
    void setUp() {
        truncator = new TelegramTextTruncator();
    }

    @Test
    void truncate_leavesShortText() {
        assertThat(truncator.truncate("hello")).isEqualTo("hello");
    }

    @Test
    void truncate_capsAt4096() {
        String longText = "x".repeat(5000);

        assertThat(truncator.truncate(longText))
                .hasSize(TelegramTextTruncator.TELEGRAM_MESSAGE_TEXT_MAX)
                .endsWith("…");
    }
}
