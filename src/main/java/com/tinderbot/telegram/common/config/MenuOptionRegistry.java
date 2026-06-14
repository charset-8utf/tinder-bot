package com.tinderbot.telegram.common.config;

import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MenuOptionRegistry {

    private final Map<String, MenuOption> byCallback;

    public MenuOptionRegistry() {
        this.byCallback = Arrays.stream(MenuOption.values())
                .collect(Collectors.toUnmodifiableMap(MenuOption::getCallback, Function.identity()));
    }

    public Optional<MenuOption> findByCallback(String callback) {
        return Optional.ofNullable(byCallback.get(callback));
    }
}
