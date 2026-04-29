package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.UserPreferenceResponse;
import com.trip4hanoi.app.entity.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    UserPreferenceResponse toUserPreferenceResponse(UserPreference userPreference);
}
