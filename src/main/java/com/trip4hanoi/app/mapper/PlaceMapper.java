package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.PlaceRequest;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ReviewMapper.class, EventMapper.class})
public interface PlaceMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    PlaceResponse toPlaceResponse(Place place);

    @Mapping(source = "category.name", target = "categoryName")
    PlaceDetailResponse toPlaceDetailResponse(Place place);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "ratingAvg", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "favoriteCount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    @Mapping(target = "savedByUsers", ignore = true)
    @Mapping(target = "posts", ignore = true)
    Place toPlace(PlaceRequest request);
}
