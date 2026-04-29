package com.trip4hanoi.app.dto.req;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceRequest {
    private String name;
    private String description;
    private Long categoryId;
    private String address;
    private String district;
    private Double latitude;
    private Double longitude;
    private Integer priceAvg;
    private String imageUrl;
}
