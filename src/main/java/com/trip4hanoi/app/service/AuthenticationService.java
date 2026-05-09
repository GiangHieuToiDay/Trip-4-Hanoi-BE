package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.GoogleLoginRequest;
import com.trip4hanoi.app.dto.req.LoginRequest;

import com.nimbusds.jose.JOSEException;
import com.trip4hanoi.app.dto.req.RefreshTokenRequest;
import com.trip4hanoi.app.dto.res.LoginResponse;

import java.text.ParseException;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(String token);
    LoginResponse refreshToken(RefreshTokenRequest token) throws ParseException, JOSEException;
    void verifyEmail(String token);
    void resendVerifyMail(String email);
    LoginResponse loginGoogle(GoogleLoginRequest request);
}
