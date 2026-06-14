package com.tinderbot.telegram.dto;

import com.tinderbot.telegram.model.DialogMode;

public record SessionResponse(
        Long chatId,
        DialogMode currentMode,
        int profileStep,
        int openerStep,
        String currentStarKey,
        int dateMessageCount,
        int messageHistorySize,
        int chatGptHistorySize
) {
}
