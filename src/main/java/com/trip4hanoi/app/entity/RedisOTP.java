package com.trip4hanoi.app.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("password_otp")
public class RedisOTP {
    @Id
    private  String email;

    @Indexed
    private String otp;

    private Long createAt;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expirationTime;
}
