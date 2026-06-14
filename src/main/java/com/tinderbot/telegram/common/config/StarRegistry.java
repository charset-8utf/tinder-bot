package com.tinderbot.telegram.common.config;

import com.tinderbot.telegram.model.Star;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class StarRegistry {

    public Optional<Star> findByKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(Star.values())
                .filter(star -> star.getPhotoKey().equals(key) || star.name().toLowerCase().contains(key))
                .findFirst();
    }
}
