package com.tinderbot.telegram.service.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiChatCompletionParserTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private OpenAiChatCompletionParser parser;

    @BeforeEach
    void setUp() {
        parser = new OpenAiChatCompletionParser();
    }

    @Test
    void prefersContentOverReasoning() {
        String json = """
                {"choices":[{"message":{"role":"assistant","content":"Hi","reasoning_content":"think"}}]}
                """;
        assertThat(parser.assistantReply(mapper.readTree(json))).isEqualTo("Hi");
    }

    @Test
    void fallsBackToLastReasoningBlockWhenContentNull() {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null,"
                + "\"reasoning_content\":\"1. Plan\\n\\nFinal reply to user.\"}}]}";
        assertThat(parser.assistantReply(mapper.readTree(json))).isEqualTo("Final reply to user.");
    }
}
