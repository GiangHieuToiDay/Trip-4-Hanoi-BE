package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.SavedPlaceResponse;
import com.trip4hanoi.app.entity.SavedPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SavedPlaceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    SavedPlaceResponse toSavedPlaceResponse(SavedPlace savedPlace);
}
