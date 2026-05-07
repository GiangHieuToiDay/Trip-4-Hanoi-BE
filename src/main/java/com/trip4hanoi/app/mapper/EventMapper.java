package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    @Mapping(target = "status", expression = "java(calculateStatus(event))")
    EventResponse toEventResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "userEventFollows", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    Event toEvent(EventRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "userEventFollows", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEvent(@org.mapstruct.MappingTarget Event event, EventRequest request);

    /**
     * lấy thời gian thực (LocalDateTime.now()) để so sánh với thời điểm bắt đầu/kết thúc của sự kiện và trả status
     * @param event
     * @return
     */
    default String calculateStatus(Event event) {
        LocalDateTime now = java.time.LocalDateTime.now();
        if (now.isBefore(event.getStartTime())) return "UPCOMING";
        if (now.isAfter(event.getEndTime())) return "ENDED";
        return "ONGOING";
    }
}
