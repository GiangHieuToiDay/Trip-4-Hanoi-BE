package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
