package com.trip4hanoi.app.dto.req;

import lombok.*;
import java.time.LocalDateTime;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    private String name;
    private String description;
    private Long placeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private List<Long> keepImageIds; // Danh sách ID ảnh cũ muốn giữ lại
}
