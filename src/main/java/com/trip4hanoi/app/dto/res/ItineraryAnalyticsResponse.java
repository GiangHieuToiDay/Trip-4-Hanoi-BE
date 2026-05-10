package com.trip4hanoi.app.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryAnalyticsResponse {  //(Lịch trình & Hành vi)
    private double avgTripDuration;  // Số ngày trung bình khách đi du lịch
    private double avgCompletionRate;// % hoàn thành lịch trình thực tế
}
