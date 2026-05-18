package com.trip4hanoi.app.mapper;

import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.DayItineraryResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.entity.Itinerary;
import com.trip4hanoi.app.entity.ItineraryPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ItineraryPlaceMapper.class})
public abstract class ItineraryMapper {

    @Autowired
    protected ItineraryPlaceMapper itineraryPlaceMapper;

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userName")
    @Mapping(source = "itineraryPlaces", target = "itineraryDays", qualifiedByName = "mapItineraryPlacesToDays")
    public abstract ItineraryResponse toItineraryResponse(Itinerary itinerary);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    public abstract Itinerary toItinerary(ItineraryRequest request);

    @Named("mapItineraryPlacesToDays")
    protected List<DayItineraryResponse> mapItineraryPlacesToDays(List<ItineraryPlace> itineraryPlaces) {
        if (itineraryPlaces == null || itineraryPlaces.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, List<ItineraryPlace>> groupedByDay = itineraryPlaces.stream()
                .collect(Collectors.groupingBy(ItineraryPlace::getDayNumber));

        return groupedByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> DayItineraryResponse.builder()
                        .dayNumber(entry.getKey())
                        .places(entry.getValue().stream()
                                .sorted(Comparator.comparing(ItineraryPlace::getOrderIndex))
                                .map(itineraryPlaceMapper::toItineraryPlaceResponse)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
