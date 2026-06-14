package com.tinderbot.telegram.api.session;

public interface DateSessionStore {

    String getCurrentStarKey(Long chatId);

    void setCurrentStarKey(Long chatId, String starKey);

    int getDateMessageCount(Long chatId);

    void incrementDateMessageCount(Long chatId);

    void resetDateMessageCount(Long chatId);
}
