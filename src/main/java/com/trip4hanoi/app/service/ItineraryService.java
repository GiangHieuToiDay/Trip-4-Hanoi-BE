package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.req.ItineraryUpdateFullRequest;
import com.trip4hanoi.app.dto.req.SaveAIItineraryRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import java.util.List;

public interface ItineraryService {
    ItineraryResponse createItinerary(ItineraryRequest request, Long userId);
    ItineraryResponse addPlaceToItinerary(ItineraryPlaceRequest request);
    List<ItineraryResponse> getUserItineraries(Long userId);

    ItineraryResponse updateItinerary(ItineraryRequest request, long id);

    ItineraryResponse updatePlaceInItinerary(ItineraryPlaceRequest request);

    ItineraryResponse removePlaceFromItinerary(Long itineraryPlaceId);

    void deleteItinerary(Long itineraryId);

    ItineraryResponse updateFull(ItineraryUpdateFullRequest request);

    ItineraryResponse saveAIItinerary(SaveAIItineraryRequest request, Long userId);

    ItineraryResponse getDetail(long id);

    ItineraryResponse reorderPlace(Long itineraryPlaceId, int newDay, int newOrderIndex);

    ItineraryResponse cloneItinerary(Long itineraryId, Long userId);

}
