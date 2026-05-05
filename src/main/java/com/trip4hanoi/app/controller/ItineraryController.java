package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.req.ItineraryUpdateFullRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.service.ItineraryService;
import jakarta.validation.Valid;
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
            @Valid
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
    public ResponseEntity<APIResponse<ItineraryResponse>> addPlaceToItinerary(
            @RequestBody ItineraryPlaceRequest request) {

        ItineraryResponse result = itineraryService.addPlaceToItinerary(request);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
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

    @PutMapping("/update-place")
    public ResponseEntity<APIResponse<ItineraryResponse>> updatePlaceInItinerary(
            @RequestBody ItineraryPlaceRequest request) {

        ItineraryResponse result = itineraryService.updatePlaceInItinerary(request);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully updated place in itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-place/{id}")
    public ResponseEntity<APIResponse<ItineraryResponse>> removePlaceFromItinerary(
            @PathVariable Long id) {

        ItineraryResponse result = itineraryService.removePlaceFromItinerary(id);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully removed place from itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-itinerary/{id}")
    public ResponseEntity<APIResponse<ItineraryResponse>> updateItinerary(
            @RequestBody ItineraryRequest request,
            @PathVariable Long id
    ){
        ItineraryResponse result = itineraryService.updateItinerary(request,id);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully update itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-itinerary/{id}")
    public ResponseEntity<APIResponse<Void>> removeItinerary(
            @PathVariable Long id) {

        itineraryService.deleteItinerary(id);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully removed itinerary")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-full")
    public ResponseEntity<APIResponse<ItineraryResponse>> updateFull(
            @RequestBody ItineraryUpdateFullRequest request) {

        ItineraryResponse result = itineraryService.updateFull(request);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Updated full itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<APIResponse<ItineraryResponse>> getDetail(
            @PathVariable long id) {

        ItineraryResponse result = itineraryService.getDetail(id);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get itinerary")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/reorder-place")
    public ResponseEntity<APIResponse<ItineraryResponse>> reorderPlace(
            @RequestParam Long itineraryPlaceId,
            @RequestParam int dayNumber,
            @RequestParam int orderIndex
    ) {

        ItineraryResponse result = itineraryService.reorderPlace(itineraryPlaceId, dayNumber, orderIndex);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Reordered successfully")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/clone/{id}")
    public ResponseEntity<APIResponse<ItineraryResponse>> cloneItinerary(
            @PathVariable Long id) {

        ItineraryResponse result = itineraryService.cloneItinerary(id);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Cloned successfully")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }


}
