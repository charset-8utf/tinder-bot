package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.GptModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GptCallbackHandler extends BaseCallbackHandler {

    public GptCallbackHandler(GptModeHandler gptModeHandler) {
        super(gptModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.GPT.getCallback());
    }
}