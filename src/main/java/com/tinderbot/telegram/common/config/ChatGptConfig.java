package com.tinderbot.telegram.common.config;

import com.plexpt.chatgpt.ChatGPT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(OpenAiTokenPresentCondition.class)
public class ChatGptConfig {

    @Bean
    public ChatGPT chatGPT(
            OpenAiApiHostResolver apiHostResolver,
            @Value("${openai.token}") String apiKey,
            @Value("${openai.api-host}") String apiHost) {
        String host = apiHostResolver.resolve(apiHost);
        return ChatGPT.builder()
                .apiKey(apiKey)
                .apiHost(host)
                .build()
                .init();
    }
}
