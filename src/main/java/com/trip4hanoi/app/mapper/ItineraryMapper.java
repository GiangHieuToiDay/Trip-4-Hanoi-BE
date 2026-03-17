package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.DayItineraryResponse;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.entity.Itinerary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItineraryMapper {
    private final ItineraryPlaceMapper itineraryPlaceMapper;

    public ItineraryResponse toItineraryResponse(Itinerary itinerary) {
        if (itinerary == null) return null;
        
        List<DayItineraryResponse> dayResponses = new ArrayList<>();
        if (itinerary.getItineraryPlaces() != null) {
            // Nhóm theo dayNumber và sắp xếp
            Map<Integer, List<ItineraryPlaceResponse>> groupedByDay = itinerary.getItineraryPlaces().stream()
                    .map(itineraryPlaceMapper::toItineraryPlaceResponse)
                    .collect(Collectors.groupingBy(
                            ItineraryPlaceResponse::getDayNumber,
                            TreeMap::new, // Dùng TreeMap để tự động sort theo số ngày 1, 2, 3...
                            Collectors.toList()
                    ));

            dayResponses = groupedByDay.entrySet().stream()
                    .map(entry -> {
                        List<ItineraryPlaceResponse> places = entry.getValue();
                        // Sắp xếp địa điểm trong ngày theo orderIndex (Sáng -> Trưa -> Chiều -> Tối)
                        places.sort(Comparator.comparing(ItineraryPlaceResponse::getOrderIndex));
                        return DayItineraryResponse.builder()
                            .dayNumber(entry.getKey())
                            .places(places)
                            .build();
                    })
                    .collect(Collectors.toList());
        }

        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .budget(itinerary.getBudget())
                .days(itinerary.getDays())
                .numberOfPeople(itinerary.getNumberOfPeople())
                .createdAt(itinerary.getCreatedAt())
                .itineraryDays(dayResponses)
                .build();
    }

    public Itinerary toItineraryEntity(ItineraryRequest request) {
        if (request == null) return null;
        return Itinerary.builder()
                .title(request.getTitle())
                .budget(request.getBudget())
                .days(request.getDays())
                .numberOfPeople(request.getNumberOfPeople())
                .build();
    }
}
