package com.tinderbot.telegram.common.config;

import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MenuOptionConfig {

    @Bean
    public Map<MenuOption, DialogMode> optionToMode() {
        return Map.of(
                MenuOption.PROFILE, DialogMode.PROFILE,
                MenuOption.OPENER, DialogMode.OPENER,
                MenuOption.MESSAGE, DialogMode.MESSAGE,
                MenuOption.DATE, DialogMode.DATE,
                MenuOption.GPT, DialogMode.GPT
        );
    }
}