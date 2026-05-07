package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.PlaceRequest;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.trip4hanoi.app.dto.res.ImageResponse;
import com.trip4hanoi.app.entity.PlaceImage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {ReviewMapper.class, EventMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PlaceMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    PlaceResponse toPlaceResponse(Place place);

    @Mapping(source = "category.name", target = "categoryName")
    PlaceDetailResponse toPlaceDetailResponse(Place place);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    @Mapping(target = "savedByUsers", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "images", ignore = true)
    Place toPlace(PlaceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    @Mapping(target = "savedByUsers", ignore = true)
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "images", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePlace(@org.mapstruct.MappingTarget Place place, PlaceRequest request);

    ImageResponse toImageResponse(PlaceImage image);
}
