package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.res.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
