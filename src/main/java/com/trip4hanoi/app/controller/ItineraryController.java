package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;

    @PostMapping("/create")
    public ResponseEntity<ItineraryResponse> createItinerary(
            @RequestBody ItineraryRequest request,
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(itineraryService.createItinerary(request, userId));
    }

    @PostMapping("/add-place")
    public ResponseEntity<ItineraryPlaceResponse> addPlaceToItinerary(
            @RequestBody ItineraryPlaceRequest request) {
        return ResponseEntity.ok(itineraryService.addPlaceToItinerary(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ItineraryResponse>> getUserItineraries(
            @RequestHeader("User-Id") Long userId) {
        return ResponseEntity.ok(itineraryService.getUserItineraries(userId));
    }
}
