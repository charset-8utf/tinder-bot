package com.tinderbot.telegram.common.config;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OpenAiApiHostResolver {

    public String resolve(String configuredHost) {
        if (!StringUtils.hasText(configuredHost)) {
            return configuredHost;
        }
        String host = configuredHost.trim();
        return host.endsWith("/") ? host : host + "/";
    }
}
