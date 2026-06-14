package com.tinderbot.telegram.dto;

import com.tinderbot.telegram.model.DialogMode;
import jakarta.validation.constraints.NotNull;

public record UpdateSessionModeRequest(
        @NotNull DialogMode mode
) {
}
