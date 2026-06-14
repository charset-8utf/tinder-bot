package com.tinderbot.telegram.entity;

import java.io.Serializable;

public record StoredGptMessage(String role, String content) implements Serializable {

    public StoredGptMessage {
        role = role != null ? role : "";
        content = content != null ? content : "";
    }
}
