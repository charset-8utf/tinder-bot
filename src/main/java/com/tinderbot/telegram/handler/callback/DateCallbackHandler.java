package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.DateModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DateCallbackHandler extends BaseCallbackHandler {

    public DateCallbackHandler(DateModeHandler dateModeHandler) {
        super(dateModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.DATE.getCallback());
    }
}