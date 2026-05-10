package com.trip4hanoi.app.dto.res;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse { //(Tổng quan hệ thống
    private long totalUsers;
    private long totalPlaces;
    private long totalPosts;
    private long totalItineraries;
    private Map<String, Long> usersByRole;// {"TRAVELER": 100, "ADMIN": 2}
    private double conversionRate; // Tỉ lệ % người dùng có Post/Itinerary
    private List<LocationCoordinateDTO> heatmap; //dữ liệu cho bản đồ nhiệt
}

