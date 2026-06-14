package com.tinderbot.telegram.mapper;

import com.tinderbot.telegram.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserInfoMapper {

    UserInfo copy(UserInfo source);
}
