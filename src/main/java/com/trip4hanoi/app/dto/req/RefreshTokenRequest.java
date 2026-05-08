package com.trip4hanoi.app.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "TOKEN_IS_EMPTY")
    private String refreshToken;
}
