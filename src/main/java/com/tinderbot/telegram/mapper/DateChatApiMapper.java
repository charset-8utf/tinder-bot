package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.dto.DateChatResponse;
import com.tinderbot.telegram.model.DateMessageResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DateChatApiMapper {

    default DateChatResponse toResponse(DateMessageResult result) {
        if (result.chatResult().limitExceeded()) {
            return DateChatResponse.limitExceeded(result.messagesUsed(), result.messagesLimit());
        }
        if (result.chatResult().reply().failed()) {
            return DateChatResponse.generationFailed(
                    result.chatResult().typingMessage(),
                    result.messagesUsed(),
                    result.messagesLimit());
        }
        return DateChatResponse.replied(
                result.chatResult().reply().text().orElse(""),
                result.chatResult().typingMessage(),
                result.messagesUsed(),
                result.messagesLimit());
    }
}
