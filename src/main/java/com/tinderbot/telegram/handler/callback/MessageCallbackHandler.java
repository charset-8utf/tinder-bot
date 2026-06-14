package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.MessageModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MessageCallbackHandler extends BaseCallbackHandler {

    public MessageCallbackHandler(MessageModeHandler messageModeHandler) {
        super(messageModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.MESSAGE.getCallback());
    }
}