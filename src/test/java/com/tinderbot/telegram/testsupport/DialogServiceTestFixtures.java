package com.tinderbot.telegram.testsupport;

import com.tinderbot.telegram.api.IChatGPTService;
import com.tinderbot.telegram.api.ISessionService;
import com.tinderbot.telegram.common.config.StarRegistry;
import com.tinderbot.telegram.service.dialog.DateDialogService;
import com.tinderbot.telegram.service.dialog.MessageDialogService;
import com.tinderbot.telegram.service.llm.TextGenerationResults;
import com.tinderbot.telegram.view.MessageView;

public final class DialogServiceTestFixtures {

    private DialogServiceTestFixtures() {
    }

    public static TextGenerationResults generationResults() {
        return new TextGenerationResults();
    }

    public static DateDialogService dateDialogService(
            ISessionService sessionService,
            IChatGPTService chatGPTService,
            MessageView messageView) {
        return new DateDialogService(
                sessionService,
                sessionService,
                sessionService,
                chatGPTService,
                messageView,
                generationResults(),
                new StarRegistry());
    }

    public static MessageDialogService messageDialogService(
            ISessionService sessionService,
            IChatGPTService chatGPTService,
            MessageView messageView) {
        return new MessageDialogService(
                sessionService,
                sessionService,
                chatGPTService,
                messageView,
                generationResults());
    }
}

