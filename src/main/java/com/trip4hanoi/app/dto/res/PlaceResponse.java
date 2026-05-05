package com.trip4hanoi.app.dto.res;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceResponse {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String address;
    private String district;
    private Double latitude;
    private Double longitude;
    private Integer priceAvg;
    private Double ratingAvg;
    private Integer viewCount;
    private Integer favoriteCount;
    private String imageUrl;


    private Double distance;// khoảng cách giữa các điểm -  Tính bằng km
    private Boolean isRecommended;//  [TODO: USER_AUTH] Đánh dấu dựa trên sở thích
    private Boolean hasActiveEvent;
}
