package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventResponse toEventResponse(Event event) {
        if (event == null) return null;
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .placeId(event.getPlace() != null ? event.getPlace().getId() : null)
                .placeName(event.getPlace() != null ? event.getPlace().getName() : null)
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .build();
    }
}
