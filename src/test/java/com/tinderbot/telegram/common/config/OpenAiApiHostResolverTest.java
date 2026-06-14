package com.tinderbot.telegram.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiApiHostResolverTest {

    private OpenAiApiHostResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new OpenAiApiHostResolver();
    }

    @Test
    void resolve_returnsConfiguredHost() {
        assertThat(resolver.resolve("https://api.openai.com/"))
                .isEqualTo("https://api.openai.com/");
    }

    @Test
    void resolve_customProxyHost_keepsHost() {
        assertThat(resolver.resolve("https://my-proxy.example/v1/"))
                .isEqualTo("https://my-proxy.example/v1/");
    }

    @Test
    void resolve_hostWithoutTrailingSlash_appendsSlash() {
        assertThat(resolver.resolve("https://api.openai.com"))
                .isEqualTo("https://api.openai.com/");
    }

    @Test
    void resolve_blankHost_returnsBlank() {
        assertThat(resolver.resolve("")).isEmpty();
    }
}
