package com.trip4hanoi.app.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Cấu hình Cache Manager cho Spring Boot 3.
     * Sử dụng Dependency Injection để lấy ObjectMapper chuẩn (Jackson 2) từ hệ thống.
     */
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        // Tạo Serializer dùng ObjectMapper của hệ thống
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Cấu hình mặc định cho các cache dùng @Cacheable
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Xây dựng Cache Manager
        // giúp tránh hoàn toàn lỗi ép kiểu LinkedHashMap.
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("personalized_recommendations",
                    config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("dashboard_v2",
                    config.entryTtl(Duration.ofHours(24))) // Cache dashboard 24h để tiết kiệm hạn mức Gemini API
                .build();
    }
}
