package com.tinderbot.telegram.model;

import com.tinderbot.telegram.common.util.ResourceConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Star {
    GRANDE(ResourceConstants.IMAGE_DATE_GRANDE, "Ариана Гранде", ResourceConstants.PROMPT_DATE_GRANDE, 5, true),
    ROBBIE(ResourceConstants.IMAGE_DATE_ROBBIE, "Марго Робби", ResourceConstants.PROMPT_DATE_ROBBIE, 8, true),
    ZENDAYA(ResourceConstants.IMAGE_DATE_ZENDAYA, "Зендея", ResourceConstants.PROMPT_DATE_ZENDAYA, 6, true),
    GOSLING(ResourceConstants.IMAGE_DATE_GOSLING, "Райан Гослинг", ResourceConstants.PROMPT_DATE_GOSLING, 7, false),
    HARDY(ResourceConstants.IMAGE_DATE_HARDY, "Том Харди", ResourceConstants.PROMPT_DATE_HARDY, 10, false);

    private final String photoKey;
    private final String name;
    private final String promptKey;
    private final int messageLimit;
    private final boolean female;
}
