package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
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

//    @GetMapping
//    public ResponseEntity<List<EventResponse>> getAllEvents() {
//        return ResponseEntity.ok(eventService.getAllEvents());
//    }

    @Operation(summary = "Get all events", description = "API get all available events")
    @GetMapping
    public ResponseEntity<APIResponse<List<EventResponse>>> getAllEvents() {

        List<EventResponse> events = eventService.getAllEvents();

        APIResponse<List<EventResponse>> response = APIResponse.<List<EventResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved events")
                .data(events)
                .build();

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/follow")
//    public ResponseEntity<String> followEvent(
//            @RequestBody EventFollowRequest request,
//            @RequestHeader("User-Id") Long userId) {
//        eventService.followEvent(request, userId);
//        return ResponseEntity.ok("Followed successfully");
//    }

    //@Operation(summary = "Follow event", description = "API for user to follow an event")
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
}
