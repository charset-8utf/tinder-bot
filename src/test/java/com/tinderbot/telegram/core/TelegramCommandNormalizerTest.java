package com.tinderbot.telegram.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramCommandNormalizerTest {

    private TelegramCommandNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new TelegramCommandNormalizer();
    }

    @Test
    void normalize_stripsAtSuffixAndPayload() {
        assertThat(normalizer.normalize("/start@MyBot payload")).isEqualTo("/start");
        assertThat(normalizer.normalize("  /gpt")).isEqualTo("/gpt");
    }
}
