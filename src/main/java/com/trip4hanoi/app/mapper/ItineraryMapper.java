package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.entity.Itinerary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItineraryPlaceMapper.class})
public interface ItineraryMapper {

    @Mapping(source = "itineraryPlaces", target = "itineraryDays")
    ItineraryResponse toItineraryResponse(Itinerary itinerary);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    Itinerary toItineraryEntity(ItineraryRequest request);
}
