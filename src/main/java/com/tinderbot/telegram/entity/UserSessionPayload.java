package com.tinderbot.telegram.entity;

import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserSessionPayload implements Serializable {

    private DialogMode currentMode = DialogMode.MAIN;
    private Integer photoMessageId;
    private Integer welcomeMessageId;
    private Integer currentMenuMessageId;
    private List<Integer> botMessageIds = new ArrayList<>();
    private String currentStarKey;
    private List<String> messageHistory = new ArrayList<>();
    private int profileStep;
    private UserInfo profileTemp;
    private int openerStep;
    private UserInfo openerTemp;
    private int dateMessageCount;
    private List<StoredGptMessage> chatGptHistory = new ArrayList<>();
}
