package com.tinderbot.telegram.service.llm;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class OpenAiChatCompletionParser {

    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_REASONING_CONTENT = "reasoning_content";

    public String assistantReply(JsonNode root) {
        JsonNode message = root.path("choices").path(0).path("message");
        String content = textIfPresent(message, FIELD_CONTENT);
        if (!content.isBlank()) {
            return content;
        }
        String reasoning = textIfPresent(message, FIELD_REASONING_CONTENT);
        if (!reasoning.isBlank()) {
            return userFacingFromReasoning(reasoning);
        }
        return "";
    }

    public boolean isFieldBlank(JsonNode parent, String field) {
        return textIfPresent(parent, field).isBlank();
    }

    public boolean hasNonBlankField(JsonNode parent, String field) {
        return !isFieldBlank(parent, field);
    }

    private String textIfPresent(JsonNode parent, String field) {
        if (!parent.has(field) || parent.get(field).isNull()) {
            return "";
        }
        return parent.get(field).stringValue("").trim();
    }

    public String userFacingFromReasoning(String reasoning) {
        String t = reasoning.strip();
        if (t.isEmpty()) {
            return "";
        }
        String[] blocks = t.split("\n\n");
        for (int i = blocks.length - 1; i >= 0; i--) {
            String block = blocks[i].strip();
            if (block.length() >= 8) {
                return block.length() > 4000 ? block.substring(block.length() - 4000) : block;
            }
        }
        return t.length() > 4000 ? t.substring(t.length() - 4000) : t;
    }
}
