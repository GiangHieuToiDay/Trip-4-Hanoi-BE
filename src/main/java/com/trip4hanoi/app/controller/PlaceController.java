package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

//    @GetMapping
//    public ResponseEntity<List<PlaceResponse>> getAllPlaces(
//            @RequestParam(required = false) Long categoryId) {
//        return ResponseEntity.ok(placeService.getAllPlaces(categoryId));
//    }

    //@Operation(summary = "Get all places", description = "API get all places with optional category filter")
    @GetMapping
    public ResponseEntity<APIResponse<List<PlaceResponse>>> getAllPlaces(
            @RequestParam(required = false) Long categoryId) {

        List<PlaceResponse> places = placeService.getAllPlaces(categoryId);

        APIResponse<List<PlaceResponse>> response = APIResponse.<List<PlaceResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved places")
                .data(places)
                .build();

        return ResponseEntity.ok(response);
    }

    //@Operation(summary = "Get place detail", description = "API get detailed information of a place")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long id) {
        PlaceDetailResponse detail = placeService.getPlaceDetail(id);

        APIResponse<PlaceDetailResponse> response = APIResponse.<PlaceDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved place detail")
                .data(detail)
                .build();

        return ResponseEntity.ok(response);
    }
}
