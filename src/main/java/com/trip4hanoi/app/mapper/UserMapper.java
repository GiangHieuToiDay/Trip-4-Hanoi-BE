package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.UserCreateRequest;
import com.trip4hanoi.app.dto.req.UserUpdateRequest;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //MapStruct
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    User toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user , UserUpdateRequest request);
}
