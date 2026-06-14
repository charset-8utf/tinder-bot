package com.tinderbot.telegram.service.llm;

import com.tinderbot.telegram.model.TextGenerationResult;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TextGenerationResults {

    public TextGenerationResult success(String text) {
        return new TextGenerationResult(Optional.of(text), false, false);
    }

    public TextGenerationResult failure() {
        return new TextGenerationResult(Optional.empty(), true, false);
    }

    public TextGenerationResult emptyHistory() {
        return new TextGenerationResult(Optional.empty(), false, true);
    }
}
