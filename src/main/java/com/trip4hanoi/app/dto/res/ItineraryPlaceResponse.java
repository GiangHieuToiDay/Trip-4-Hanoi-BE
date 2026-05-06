package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlaceResponse {
    private Long id;
    private Long placeId;
    private String placeName;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer dayNumber;
    private Integer orderIndex;
    private String session;
    private Integer estimatedCost;

    private EventResponse eventInfo; //Thông tin event nếu có
}
