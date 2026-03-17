package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .nationality(user.getNationality())
                .language(user.getLanguage())
                .build();
    }
}
