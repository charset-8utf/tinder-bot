package com.tinderbot.telegram.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserSession {
    private DialogMode currentMode = DialogMode.MAIN;
    private Integer photoMessageId;
    private Integer welcomeMessageId;
    private Integer currentMenuMessageId;
    private final List<Integer> botMessageIds = new ArrayList<>();
    private String currentStarKey;
    private final List<String> messageHistory = new ArrayList<>();

    private int profileStep = 0;
    private UserInfo profileTemp;

    private int openerStep = 0;
    private UserInfo openerTemp;
    private int dateMessageCount = 0;
    private final List<com.plexpt.chatgpt.entity.chat.Message> chatGptHistory = new ArrayList<>();

    public void addBotMessageId(Integer messageId) {
        botMessageIds.add(messageId);
    }

    public List<Integer> getBotMessageIds() {
        return new ArrayList<>(botMessageIds);
    }

    public void clearBotMessageIds() {
        botMessageIds.clear();
    }

    public List<String> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    public void addMessageToHistory(String message) {
        messageHistory.add(message);
    }

    public void clearMessageHistory() {
        messageHistory.clear();
    }

    public List<com.plexpt.chatgpt.entity.chat.Message> getChatGptHistory() {
        return new ArrayList<>(chatGptHistory);
    }

    public void setChatGptHistory(List<com.plexpt.chatgpt.entity.chat.Message> messages) {
        chatGptHistory.clear();
        if (messages != null) {
            chatGptHistory.addAll(messages);
        }
    }

    public void clearChatGptHistory() {
        chatGptHistory.clear();
    }
}