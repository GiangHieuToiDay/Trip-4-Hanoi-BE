package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.entity.ItineraryPlace;
import org.springframework.stereotype.Component;

@Component
public class ItineraryPlaceMapper {
    public ItineraryPlaceResponse toItineraryPlaceResponse(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) return null;
        return ItineraryPlaceResponse.builder()
                .id(itineraryPlace.getId())
                .placeId(itineraryPlace.getPlace() != null ? itineraryPlace.getPlace().getId() : null)
                .placeName(itineraryPlace.getPlace() != null ? itineraryPlace.getPlace().getName() : null)
                .dayNumber(itineraryPlace.getDayNumber())
                .orderIndex(itineraryPlace.getOrderIndex())
                .session(itineraryPlace.getSession())
                .estimatedCost(itineraryPlace.getEstimatedCost())
                .build();
    }

    public ItineraryPlace toItineraryPlaceEntity(ItineraryPlaceRequest request) {
        if (request == null) return null;
        return ItineraryPlace.builder()
                .dayNumber(request.getDayNumber())
                .orderIndex(request.getOrderIndex())
                .estimatedCost(request.getEstimatedCost())
                .build();
    }
}
