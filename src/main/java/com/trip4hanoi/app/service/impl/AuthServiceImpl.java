package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.LoginRequest;
import com.trip4hanoi.app.dto.res.AuthResponse;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.UserMapper;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return AuthResponse.builder()
                .token("mock-jwt-token") // Sẽ thay thế bằng JWT thực sau
                .user(userMapper.toUserResponse(user))
                .build();
    }
}
