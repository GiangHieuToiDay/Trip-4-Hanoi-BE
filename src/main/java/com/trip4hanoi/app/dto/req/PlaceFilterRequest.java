package com.trip4hanoi.app.dto.req;


import lombok.Data;

@Data
public class PlaceFilterRequest {
    private String keyword;
    private Long categoryId;
    private String district;
    private Integer minPrice;
    private Integer maxPrice;
    private Double minRating;

    // Thông tin vị trí để "Tìm quanh đây"  (Proximity)
    private Double userLat;
    private Double userLng;
    private Double radius ; // km


    private int page = 1;  // Mặc định trang 1
    private int size = 10;
    private String sortBy = "ratingAvg"; // Mặc định sắp xếp theo rating
}
