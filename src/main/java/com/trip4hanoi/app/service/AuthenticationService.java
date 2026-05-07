package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.LoginRequest;

import com.nimbusds.jose.JOSEException;
import com.trip4hanoi.app.dto.req.RefreshTokenRequest;
import com.trip4hanoi.app.dto.res.LoginResponse;

import java.text.ParseException;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(String token) throws ParseException;
    LoginResponse refreshToken(RefreshTokenRequest token) throws ParseException, JOSEException;
}
