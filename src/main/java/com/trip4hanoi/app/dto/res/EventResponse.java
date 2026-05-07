package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private Long placeId;
    private String placeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // UPCOMING, ONGOING, ENDED
    private List<ImageResponse> images; // Danh sách album ảnh

}
