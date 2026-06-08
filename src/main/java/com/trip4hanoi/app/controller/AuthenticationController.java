package com.trip4hanoi.app.controller;


import com.nimbusds.jose.JOSEException;
import com.trip4hanoi.app.dto.req.ForgotPasswordRequest;
import com.trip4hanoi.app.dto.req.GoogleLoginRequest;
import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.req.RefreshTokenRequest;
import com.trip4hanoi.app.dto.req.ResetPasswordRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.LoginResponse;
import com.trip4hanoi.app.service.AuthenticationService;
import com.trip4hanoi.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j(topic = "AUTHENTICATION CONTROLLER")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        APIResponse<LoginResponse> response=   APIResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Login successful")
                .data(authenticationService.login(request))
                .build();

        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/login-google")
    public ResponseEntity<APIResponse<LoginResponse>> loginGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        APIResponse<LoginResponse> response = APIResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Google login successful")
                .data(authenticationService.loginGoogle(request))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        APIResponse<Void> response=   APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("logout successful")
                .build();

        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshToken) throws ParseException, JOSEException {

        LoginResponse newTokens = authenticationService.refreshToken(refreshToken);

        APIResponse<LoginResponse> response = APIResponse.<LoginResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Access token refreshed successfully")
                .data(newTokens)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.sendForgotPasswordEmail(request);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("OTP đã được gửi đến email của bạn")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Mật khẩu đã được đặt lại thành công")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/resend-verify")
    public ResponseEntity<APIResponse<String>> resendVerifyEmail(
            @RequestParam String email
    ) {

        authenticationService.resendVerifyMail(email);

        APIResponse<String> response = APIResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Verify email resent successfully")
                .data("Email sent successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<APIResponse<String>> verifyEmail(
            @RequestParam String token
    ) {

        authenticationService.verifyEmail(token);

        APIResponse<String> response = APIResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Email verified successfully")
                .data("Verify success")
                .build();

        return ResponseEntity.ok(response);
    }



}
