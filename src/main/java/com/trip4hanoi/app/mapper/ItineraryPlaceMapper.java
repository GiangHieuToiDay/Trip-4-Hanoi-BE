package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.entity.ItineraryPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItineraryPlaceMapper {

    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    @Mapping(source = "place.imageUrl", target = "imageUrl")
    @Mapping(source = "place.latitude", target = "latitude")
    @Mapping(source = "place.longitude", target = "longitude")
    @Mapping(source = "place.address", target = "address")
    ItineraryPlaceResponse toItineraryPlaceResponse(ItineraryPlace itineraryPlace);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "session", ignore = true)
    ItineraryPlace toItineraryPlace(ItineraryPlaceRequest request);
}
