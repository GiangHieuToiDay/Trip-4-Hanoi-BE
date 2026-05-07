package com.trip4hanoi.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RedisHash("auth_cache")
public class RedisAuthority {
    @Id
    private String email; // Key chính là email người dùng

    private List<String> authorities; // Danh sách ROLE_... và PERMISSION_...

    @TimeToLive
    private Long expirationTime; // Thời gian sống của cache (giây)
}
