package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.OpenerModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OpenerCallbackHandler extends BaseCallbackHandler {

    public OpenerCallbackHandler(OpenerModeHandler openerModeHandler) {
        super(openerModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.OPENER.getCallback());
    }
}