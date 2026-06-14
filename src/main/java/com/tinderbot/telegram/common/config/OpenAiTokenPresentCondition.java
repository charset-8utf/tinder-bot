package com.tinderbot.telegram.common.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class OpenAiTokenPresentCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String token = context.getEnvironment().getProperty("openai.token");
        return StringUtils.hasText(token);
    }
}
