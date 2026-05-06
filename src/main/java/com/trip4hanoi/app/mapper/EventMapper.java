package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

import com.trip4hanoi.app.dto.res.ImageResponse;
import com.trip4hanoi.app.entity.EventImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    @Mapping(target = "status", expression = "java(calculateStatus(event))")
    EventResponse toEventResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "userEventFollows", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    Event toEvent(EventRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "userEventFollows", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEvent(@org.mapstruct.MappingTarget Event event, EventRequest request);

    ImageResponse toImageResponse(EventImage image);

    /**
     * lấy thời gian thực (LocalDateTime.now()) để so sánh với thời điểm bắt đầu/kết thúc của sự kiện và trả status
     * @param event
     * @return
     */
    default String calculateStatus(Event event) {
        // Kiểm tra null để tránh NullPointerException khi startTime hoặc endTime chưa có dữ liệu
        if (event.getStartTime() == null || event.getEndTime() == null) {
            return "UPCOMING"; // Trạng thái mặc định nếu thiếu dữ liệu thời gian
        }

        LocalDateTime now = java.time.LocalDateTime.now();

        // So sánh an toàn sau khi đã check null
        if (now.isBefore(event.getStartTime())) return "UPCOMING";
        if (now.isAfter(event.getEndTime())) return "ENDED";

        return "ONGOING";
    }
}
