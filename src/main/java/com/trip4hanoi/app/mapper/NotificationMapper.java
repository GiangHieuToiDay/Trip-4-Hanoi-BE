package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.NotificationRequest;
import com.trip4hanoi.app.dto.res.NotificationResponse;
import com.trip4hanoi.app.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "targetUrl", target = "targetUrl")
    NotificationResponse toNotificationResponse(Notification notification);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Notification toNotification(NotificationRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateNotification(@MappingTarget Notification notification, NotificationRequest request);
}
