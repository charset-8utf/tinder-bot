package com.tinderbot.telegram.view;

import com.tinderbot.telegram.common.util.CallbackConstants;
import com.tinderbot.telegram.model.MenuOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class KeyboardFactory {

    public String[] createMainMenuButtons() {
        MenuOption[] options = MenuOption.values();
        return IntStream.range(0, options.length)
                .boxed()
                .flatMap(i -> Stream.of(options[i].getButtonText(), options[i].getCallback()))
                .toArray(String[]::new);
    }

    public String[] createBackToMainMenuButton() {
        return new String[]{"Главное меню бота", MenuOption.START.getCallback()};
    }

    public String[] createStarButtons() {
        List<Map.Entry<String, String>> starButtons = List.of(
                Map.entry("1. Ариана Гранде 🔥 (сложность 5/10)", "grande"),
                Map.entry("2. Марго Робби 🔥🔥 (сложность 7/10)", "robbie"),
                Map.entry("3. Зендея 🔥🔥🔥 (сложность 10/10)", "zendaya"),
                Map.entry("4. Райан Гослинг 😎 (сложность 7/10)", "gosling"),
                Map.entry("5. Том Харди 😎😎 (сложность 10/10)", "hardy")
        );
        return starButtons.stream()
                .flatMap(entry -> Stream.of(entry.getKey(), CallbackConstants.STAR_PREFIX + entry.getValue()))
                .toArray(String[]::new);
    }
}