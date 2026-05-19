package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.WeatherResponse;
import com.trip4hanoi.app.service.SmartNotificationEngine;
import com.trip4hanoi.app.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "WEATHER-ALERT-JOB")
public class WeatherAlertJob {

    private final WeatherService weatherService;
    private final SmartNotificationEngine smartNotificationEngine;

    /**
     * Chạy định kỳ mỗi 1 tiếng để kiểm tra thời tiết nguy hiểm.
     * Fixed Rate: 3600000ms = 1h
     */
    @Scheduled(fixedRate = 3600000)
    public void checkDangerousWeather() {
        log.info("Bắt đầu Job kiểm tra thời tiết khẩn cấp...");
        
        try {
            // Lấy thời tiết Hà Nội hiện tại
            WeatherResponse weather = weatherService.getWeatherByCity("Hanoi");
            
            // Chuyển cho bộ não SmartNotificationEngine xử lý
            smartNotificationEngine.processWeatherAlertTrigger(weather);
            
        } catch (Exception e) {
            log.error("Lỗi khi thực thi Job cảnh báo thời tiết: {}", e.getMessage());
        }
    }
}
