package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.UserCreateRequest;
import com.trip4hanoi.app.dto.req.UserUpdateRequest;
import com.trip4hanoi.app.dto.res.UserResponse;
import com.trip4hanoi.app.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //MapStruct
    @Mapping(target = "username", source = "actualUsername")
    @Mapping(target = "subscription.planType", source = "subscription.planType")
    @Mapping(target = "subscription.endDate", source = "subscription.endDate")
    @Mapping(target = "subscription.isActive", source = "subscription.isActive")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    User toEntity(UserCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateUser(@MappingTarget User user , UserUpdateRequest request);
}
