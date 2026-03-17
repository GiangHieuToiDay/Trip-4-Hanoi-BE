package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import java.util.List;

public interface ItineraryService {
    ItineraryResponse createItinerary(ItineraryRequest request, Long userId);
    ItineraryPlaceResponse addPlaceToItinerary(ItineraryPlaceRequest request);
    List<ItineraryResponse> getUserItineraries(Long userId);
}
