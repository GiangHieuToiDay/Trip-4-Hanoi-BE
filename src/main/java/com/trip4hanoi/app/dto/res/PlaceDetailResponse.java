package com.trip4hanoi.app.dto.res;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double ratingAvg;
    private Integer priceAvg;
    private List<ImageResponse> images; // Danh sách album ảnh
    private List<ReviewResponse> reviews;
    private List<EventResponse> events;
}
