package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;

//    @PostMapping("/create")
//    public ResponseEntity<ItineraryResponse> createItinerary(
//            @RequestBody ItineraryRequest request,
//            @RequestHeader("User-Id") Long userId) {
//        return ResponseEntity.ok(itineraryService.createItinerary(request, userId));
//    }

    //@Operation(summary = "Create itinerary", description = "API create itinerary for user")
    @PostMapping("/create")
    public ResponseEntity<APIResponse<ItineraryResponse>> createItinerary(
            @RequestBody ItineraryRequest request,
            @RequestHeader("User-Id") Long userId) {

        ItineraryResponse itinerary = itineraryService.createItinerary(request, userId);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Successfully created itinerary")
                .data(itinerary)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @PostMapping("/add-place")
//    public ResponseEntity<ItineraryPlaceResponse> addPlaceToItinerary(
//            @RequestBody ItineraryPlaceRequest request) {
//        return ResponseEntity.ok(itineraryService.addPlaceToItinerary(request));
//    }

    //@Operation(summary = "Add place to itinerary", description = "API add place into itinerary")
    @PostMapping("/add-place")
    public ResponseEntity<APIResponse<ItineraryPlaceResponse>> addPlaceToItinerary(
            @RequestBody ItineraryPlaceRequest request) {

        ItineraryPlaceResponse result = itineraryService.addPlaceToItinerary(request);

        APIResponse<ItineraryPlaceResponse> response = APIResponse.<ItineraryPlaceResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully added place to itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/my")
//    public ResponseEntity<List<ItineraryResponse>> getUserItineraries(
//            @RequestHeader("User-Id") Long userId) {
//        return ResponseEntity.ok(itineraryService.getUserItineraries(userId));
//    }

    //@Operation(summary = "Get user itineraries", description = "API get all itineraries of a user")
    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ItineraryResponse>>> getUserItineraries(
            @RequestHeader("User-Id") Long userId) {

        List<ItineraryResponse> itineraries = itineraryService.getUserItineraries(userId);

        APIResponse<List<ItineraryResponse>> response = APIResponse.<List<ItineraryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved itineraries")
                .data(itineraries)
                .build();

        return ResponseEntity.ok(response);
    }
}
