package com.trip4hanoi.app.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtInfo {
    private String jwtId;
    private Date issuedTime;
    private Date expiredTime;
}
