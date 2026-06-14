package com.tinderbot.telegram.mapper;

import com.plexpt.chatgpt.entity.chat.Message;
import com.tinderbot.telegram.entity.StoredGptMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GptMessageMapper {

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "toolCalls", ignore = true)
    Message toChatMessage(StoredGptMessage stored);

    StoredGptMessage fromChatMessage(Message message);

    List<Message> toChatMessages(List<StoredGptMessage> stored);

    List<StoredGptMessage> fromChatMessages(List<Message> messages);
}
