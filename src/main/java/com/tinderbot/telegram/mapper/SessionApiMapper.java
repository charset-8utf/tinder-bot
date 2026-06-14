package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.model.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SessionApiMapper {

    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "currentMode", source = "session.currentMode")
    @Mapping(target = "profileStep", source = "session.profileStep")
    @Mapping(target = "openerStep", source = "session.openerStep")
    @Mapping(target = "currentStarKey", source = "session.currentStarKey")
    @Mapping(target = "dateMessageCount", source = "session.dateMessageCount")
    @Mapping(target = "messageHistorySize", expression = "java(session.getMessageHistory().size())")
    @Mapping(target = "chatGptHistorySize", expression = "java(session.getChatGptHistory().size())")
    SessionResponse toResponse(Long chatId, UserSession session);
}
