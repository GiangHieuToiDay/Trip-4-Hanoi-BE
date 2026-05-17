package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.OpenWeatherMapResponse;
import com.trip4hanoi.app.dto.res.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "WEATHER-SERVICE")
public class WeatherService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private static final String CACHE_KEY_PREFIX = "weatherCache::";

    public WeatherResponse getWeatherByCity(String city) {
        String cacheKey = CACHE_KEY_PREFIX + city;

        //  Thử lấy dữ liệu từ Redis Cache trước
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.info("Lấy dữ liệu thời tiết từ Cache cho thành phố: {}", city);
                return objectMapper.readValue(cachedData, WeatherResponse.class);
            }
        } catch (Exception e) {
            log.error("Lỗi khi đọc Cache Redis: {}", e.getMessage());
        }

        //  Nếu không có cache, gọi API OpenWeatherMap
        log.info("Gọi API thực tế để lấy thời tiết cho thành phố: {}", city);
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=vi", apiUrl, city, apiKey);

        WeatherResponse weatherResponse;
        try {
            OpenWeatherMapResponse response = restTemplate.getForObject(url, OpenWeatherMapResponse.class);

            if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
                OpenWeatherMapResponse.Weather weatherData = response.getWeather().get(0);

                weatherResponse = WeatherResponse.builder()
                        .temp(Math.round(response.getMain().getTemp()))
                        .condition(weatherData.getMain())
                        .description(weatherData.getDescription())
                        .iconCode(weatherData.getIcon())
                        .build();

                //  Lưu vào Redis Cache (Cache trong 60 phút)
                try {
                    String jsonValue = objectMapper.writeValueAsString(weatherResponse);
                    redisTemplate.opsForValue().set(cacheKey, jsonValue, 60, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("Lỗi khi lưu dữ liệu vào Cache: {}", e.getMessage());
                }

                return weatherResponse;
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi OpenWeatherMap API: {}", e.getMessage());
        }

        // Fallback: Dữ liệu mặc định nếu mọi thứ thất bại
        return WeatherResponse.builder()
                .temp(25.0)
                .condition("Clear")
                .description("Có nắng")
                .iconCode("01d")
                .build();
    }
}
