package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceMapper {
    private final ReviewMapper reviewMapper;
    private final EventMapper eventMapper;

    public PlaceResponse toPlaceResponse(Place place) {
        if (place == null) return null;
        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .categoryName(place.getCategory() != null ? place.getCategory().getName() : null)
                .address(place.getAddress())
                .ratingAvg(place.getRatingAvg())
                .priceAvg(place.getPriceAvg())
                .imageUrl(place.getImageUrl())
                .build();
    }

    public PlaceDetailResponse toPlaceDetailResponse(Place place) {
        if (place == null) return null;
        return PlaceDetailResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .categoryName(place.getCategory() != null ? place.getCategory().getName() : null)
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .ratingAvg(place.getRatingAvg())
                .priceAvg(place.getPriceAvg())
                .imageUrl(place.getImageUrl())
                .reviews(place.getReviews() != null ? 
                    place.getReviews().stream().map(reviewMapper::toReviewResponse).collect(Collectors.toList()) : null)
                .events(place.getEvents() != null ? 
                    place.getEvents().stream().map(eventMapper::toEventResponse).collect(Collectors.toList()) : null)
                .build();
    }
}
