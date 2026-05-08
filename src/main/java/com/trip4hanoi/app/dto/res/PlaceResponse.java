package com.trip4hanoi.app.dto.res;

import lombok.*;

import java.util.List;

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
    
    private List<ImageResponse> images; // Danh sách album ảnh


    @Builder.Default
    private Double distance = 0.0;// khoảng cách giữa các điểm -  Tính bằng km
    @Builder.Default
    private Boolean isRecommended = false;//  [TODO: USER_AUTH] Đánh dấu dựa trên sở thích
    @Builder.Default
    private Boolean hasActiveEvent = false;
}
