package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<List<PlaceResponse>> getAllPlaces(
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(placeService.getAllPlaces(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDetailResponse> getPlaceDetail(@PathVariable Long id) {
        return ResponseEntity.ok(placeService.getPlaceDetail(id));
    }
}
