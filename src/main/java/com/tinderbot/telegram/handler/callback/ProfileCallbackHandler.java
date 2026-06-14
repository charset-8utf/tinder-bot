package com.tinderbot.telegram.handler.callback;

import com.tinderbot.telegram.handler.mode.ProfileModeHandler;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProfileCallbackHandler extends BaseCallbackHandler {

    public ProfileCallbackHandler(ProfileModeHandler profileModeHandler) {
        super(profileModeHandler);
    }

    @Override
    public boolean supports(String callback) {
        return Objects.equals(callback, MenuOption.PROFILE.getCallback());
    }
}