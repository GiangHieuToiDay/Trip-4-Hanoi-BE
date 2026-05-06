package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    /**
     * ENDPOINT - USER (hiển thị event đáng và sắp diễn ra)
     * @param keyword
     * @param placeId
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<EventResponse>>> getAllEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long placeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<EventResponse> result = eventService.getAllEventsUser(keyword, placeId, page, size);
        APIResponse<PageResponse<EventResponse>> response = APIResponse.<PageResponse<EventResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved events")
                .data(result)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/follow")
    public ResponseEntity<APIResponse<String>> followEvent(
            @RequestBody EventFollowRequest request,
            @RequestHeader("User-Id") Long userId) {
        eventService.followEvent(request, userId);
        APIResponse<String> response = APIResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Followed successfully")
                .data("Followed successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<APIResponse<EventResponse>> createEvent(@RequestBody EventRequest request) {
        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<EventResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Successfully created event")
                .data(event)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<EventResponse>> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(APIResponse.<EventResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully updated event")
                .data(event)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully deleted event")
                .build());
    }

    /**
     * ENDPOINT - ADMIN
     * @param keyword
     * @param placeId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/admin")
    public ResponseEntity<APIResponse<PageResponse<EventResponse>>> getAllEventsAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long placeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<EventResponse> result = eventService.getAllEventsAdmin(keyword, placeId, page, size);
        return ResponseEntity.ok(APIResponse.<PageResponse<EventResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved events for admin")
                .data(result)
                .build());
    }
}
