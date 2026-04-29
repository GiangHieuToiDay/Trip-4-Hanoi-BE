package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    EventResponse toEventResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "userEventFollows", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    Event toEvent(EventRequest request);
}
