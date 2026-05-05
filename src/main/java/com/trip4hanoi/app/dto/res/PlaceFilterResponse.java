package com.trip4hanoi.app.dto.res;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceFilterResponse {
    private Long id;
    private String name;
    private String address;
    private String district;
    private String imageUrl;
    private Double ratingAvg;
    private Integer priceAvg;

    private Double distance; // Khoảng cách tính toán được (km)
    private Boolean isRecommended; // [TODO: USER_AUTH] - Sẽ đánh dấu sau khi có Login
}
