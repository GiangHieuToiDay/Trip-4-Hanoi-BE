package com.trip4hanoi.app.service;

public interface GeocodingService {
    /**
     * Xác định tên Quận/Huyện dựa trên tọa độ Lat/Lng
     * Trả về "Hà Nội" nếu không khớp quận nào hoặc null nếu nằm ngoài Hà Nội
     */
    String getDistrictFromCoords(Double lat, Double lng);
}
