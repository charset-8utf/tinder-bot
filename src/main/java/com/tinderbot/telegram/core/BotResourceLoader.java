package com.tinderbot.telegram.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class BotResourceLoader {

    public String loadPrompt(String name) {
        return loadText("prompts/" + name + ".txt", "GPT-промпт «" + name + "»");
    }

    public String loadMessage(String name) {
        return loadText("messages/" + name + ".txt", "сообщение «" + name + "»");
    }

    public InputStream loadImage(String name) {
        String path = "images/" + name + ".jpg";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw resourceNotFound("Изображение не найдено: " + path);
            }
            return resource.getInputStream();
        } catch (IOException e) {
            throw loadFailed("Не удалось загрузить изображение «" + name + "»", e);
        }
    }

    private String loadText(String path, String label) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw resourceNotFound("Ресурс не найден: " + path);
            }
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw loadFailed("Не удалось загрузить " + label, e);
        }
    }

    private IllegalStateException resourceNotFound(String message) {
        log.error(message);
        return new IllegalStateException(message);
    }

    private IllegalStateException loadFailed(String message, IOException cause) {
        log.error(message, cause);
        return new IllegalStateException(message, cause);
    }
}
