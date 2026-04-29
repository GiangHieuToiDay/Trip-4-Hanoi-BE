package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.NotificationResponse;
import com.trip4hanoi.app.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    NotificationResponse toNotificationResponse(Notification notification);
}
