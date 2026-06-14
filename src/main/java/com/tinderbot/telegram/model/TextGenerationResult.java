package com.tinderbot.telegram.model;

import java.util.Optional;

public record TextGenerationResult(Optional<String> text, boolean failed, boolean emptyHistory) {

    public boolean hasEmptyHistory() {
        return emptyHistory;
    }
}
