package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {
    private Long id;
    private String name;
    private String categoryName;
    private String address;
    private Double ratingAvg;
    private Integer priceAvg;
    private String imageUrl;
}
