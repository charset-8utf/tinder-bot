package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.entity.UserSessionPayload;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.model.UserSession;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

import java.util.ArrayList;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {GptMessageMapper.class, UserInfoMapper.class},
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface UserSessionMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "currentMode", ignore = true)
    @Mapping(target = "botMessageIds", ignore = true)
    @Mapping(target = "messageHistory", ignore = true)
    @Mapping(target = "chatGptHistory", source = "chatGptHistory")
    @Mapping(target = "profileTemp", source = "profileTemp")
    @Mapping(target = "openerTemp", source = "openerTemp")
    UserSession toDomain(UserSessionPayload payload);

    @AfterMapping
    default void afterToDomain(@MappingTarget UserSession session, UserSessionPayload payload) {
        if (payload != null && payload.getCurrentMode() != null) {
            session.setCurrentMode(payload.getCurrentMode());
        } else {
            session.setCurrentMode(DialogMode.MAIN);
        }
        if (payload == null) {
            return;
        }
        if (payload.getBotMessageIds() != null) {
            payload.getBotMessageIds().forEach(session::addBotMessageId);
        }
        if (payload.getMessageHistory() != null) {
            payload.getMessageHistory().forEach(session::addMessageToHistory);
        }
    }

    @Mapping(target = "botMessageIds", ignore = true)
    @Mapping(target = "messageHistory", ignore = true)
    @Mapping(target = "chatGptHistory", source = "chatGptHistory")
    @Mapping(target = "profileTemp", source = "profileTemp")
    @Mapping(target = "openerTemp", source = "openerTemp")
    UserSessionPayload toPayload(UserSession session);

    @AfterMapping
    default void copyCollectionsToPayload(@MappingTarget UserSessionPayload payload, UserSession session) {
        if (session == null) {
            return;
        }
        payload.setBotMessageIds(new ArrayList<>(session.getBotMessageIds()));
        payload.setMessageHistory(new ArrayList<>(session.getMessageHistory()));
    }
}
