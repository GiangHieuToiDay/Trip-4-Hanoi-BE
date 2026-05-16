package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.SavedPlaceResponse;
import com.trip4hanoi.app.service.SavedPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-places")
@RequiredArgsConstructor
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;

    @PostMapping("/{placeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<SavedPlaceResponse>> toggleSavePlace(@PathVariable Long placeId) {
        SavedPlaceResponse result = savedPlaceService.toggleSavePlace(placeId);
        
        String message = (result != null) ? "Successfully saved place" : "Successfully unsaved place";
        
        return ResponseEntity.ok(
                APIResponse.<SavedPlaceResponse>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message(message)
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<List<SavedPlaceResponse>>> getMySavedPlaces() {
        List<SavedPlaceResponse> result = savedPlaceService.getMySavedPlaces();
        
        return ResponseEntity.ok(
                APIResponse.<List<SavedPlaceResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved saved places")
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/check/{placeId}")
    public ResponseEntity<APIResponse<Boolean>> isPlaceSaved(@PathVariable Long placeId) {
        boolean result = savedPlaceService.isPlaceSaved(placeId);
        
        return ResponseEntity.ok(
                APIResponse.<Boolean>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Check result")
                        .data(result)
                        .build()
        );
    }
}
