package com.tinderbot.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TinderBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(TinderBotApplication.class, args);
    }
}