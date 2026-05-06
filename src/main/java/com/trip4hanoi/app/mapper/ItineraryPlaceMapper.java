package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.entity.ItineraryPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface ItineraryPlaceMapper {

    @Mapping(source = "place.id", target = "placeId")
    @Mapping(source = "place.name", target = "placeName")
    @Mapping(target = "imageUrl", expression = "java(itineraryPlace.getPlace().getImages() != null && !itineraryPlace.getPlace().getImages().isEmpty() ? itineraryPlace.getPlace().getImages().get(0).getImageUrl() : null)")
    @Mapping(source = "place.latitude", target = "latitude")
    @Mapping(source = "place.longitude", target = "longitude")
    @Mapping(source = "place.address", target = "address")
    @Mapping(source = "event", target = "eventInfo")
    ItineraryPlaceResponse toItineraryPlaceResponse(ItineraryPlace itineraryPlace);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "estimatedCost", ignore = true)
    ItineraryPlace toItineraryPlace(ItineraryPlaceRequest request);
}
