package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.res.WeatherResponse;
import com.trip4hanoi.app.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherResponse> getCurrentWeather(
            @RequestParam(defaultValue = "Hanoi") String city) {
        WeatherResponse weather = weatherService.getWeatherByCity(city);
        return ResponseEntity.ok(weather);
    }
}
