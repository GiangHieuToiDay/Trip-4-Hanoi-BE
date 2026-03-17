package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PostMapping("/follow")
    public ResponseEntity<String> followEvent(
            @RequestBody EventFollowRequest request,
            @RequestHeader("User-Id") Long userId) {
        eventService.followEvent(request, userId);
        return ResponseEntity.ok("Followed successfully");
    }
}
