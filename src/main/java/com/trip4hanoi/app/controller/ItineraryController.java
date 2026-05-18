package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.req.ItineraryUpdateFullRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {
    private final ItineraryService itineraryService;

    @Operation(summary = "Get all itineraries for admin", description = "API get all itineraries with pagination for admin/staff")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('MODERATE_CONTENT')")
    public ResponseEntity<APIResponse<PageResponse<ItineraryResponse>>> getAllItinerariesAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isSample,
            @RequestParam(required = false) String status) {
        
        PageResponse<ItineraryResponse> result = itineraryService.getAllItinerariesAdmin(page, size, keyword, isSample, status);

        APIResponse<PageResponse<ItineraryResponse>> response = APIResponse.<PageResponse<ItineraryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved all itineraries")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<APIResponse<List<ItineraryResponse>>> getFeaturedItineraries() {
        List<ItineraryResponse> result = itineraryService.getFeaturedItineraries();

        APIResponse<List<ItineraryResponse>> response = APIResponse.<List<ItineraryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved featured itineraries")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/samples")
    public ResponseEntity<APIResponse<List<ItineraryResponse>>> getSampleItineraries() {
        List<ItineraryResponse> result = itineraryService.getSampleItineraries();

        APIResponse<List<ItineraryResponse>> response = APIResponse.<List<ItineraryResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved sample itineraries")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create itinerary", description = "API create itinerary for user")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ItineraryResponse>> createItinerary(
            @Valid
            @RequestBody ItineraryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id") instanceof Number n ? n.longValue() : null;
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

    @Operation(summary = "Add place to itinerary", description = "API add place into itinerary")
    @PostMapping("/add-place")
    @PreAuthorize("isAuthenticated()")
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

    @Operation(summary = "Get user itineraries", description = "API get all itineraries of a user")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<List<ItineraryResponse>>> getUserItineraries(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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

    @PostMapping("/save-ai")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ItineraryResponse>> saveAIItinerary(
            @RequestBody com.trip4hanoi.app.dto.req.SaveAIItineraryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
        ItineraryResponse result = itineraryService.saveAIItinerary(request, userId);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully saved AI itinerary")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ItineraryResponse>> cloneItinerary(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
        ItineraryResponse result = itineraryService.cloneItinerary(id, userId);

        APIResponse<ItineraryResponse> response = APIResponse.<ItineraryResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Cloned successfully")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }


}
