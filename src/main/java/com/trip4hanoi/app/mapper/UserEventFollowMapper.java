package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.UserEventFollowResponse;
import com.trip4hanoi.app.entity.UserEventFollow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEventFollowMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "event.name", target = "eventName")
    UserEventFollowResponse toUserEventFollowResponse(UserEventFollow follow);
}
