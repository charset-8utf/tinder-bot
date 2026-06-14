package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.MainModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StartCallbackHandler extends BaseCallbackHandler {

    public StartCallbackHandler(MainModeHandler mainModeHandler) {
        super(mainModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.START.getCallback());
    }
}