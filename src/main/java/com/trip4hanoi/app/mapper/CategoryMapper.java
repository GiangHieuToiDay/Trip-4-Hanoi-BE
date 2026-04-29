package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.CategoryRequest;
import com.trip4hanoi.app.dto.res.CategoryResponse;
import com.trip4hanoi.app.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "places", ignore = true)
    @Mapping(target = "userPreferences", ignore = true)
    Category toCategory(CategoryRequest request);
}
