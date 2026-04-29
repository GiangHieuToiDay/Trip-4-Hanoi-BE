package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.AuthResponse;
import com.trip4hanoi.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //@Operation(summary = "User login", description = "API authenticate user and return token")
    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        APIResponse<AuthResponse> response = APIResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Login successfully")
                .data(authResponse)
                .build();

        return ResponseEntity.ok(response);
    }

}
