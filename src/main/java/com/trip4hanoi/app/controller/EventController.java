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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventController {
    private final EventService eventService;

    /**
     * ENDPOINT - USER: Lấy danh sách sự kiện đang và sắp diễn ra
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


    /**
     * ENDPOINT - USER: Theo dõi sự kiện
     */
    @PostMapping("/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<String>> followEvent(
            @RequestBody EventFollowRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
        eventService.followEvent(request, userId);
        APIResponse<String> response = APIResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Followed successfully")
                .data("Followed successfully")
                .build();
        return ResponseEntity.ok(response);
    }


    /**
     * ENDPOINT - ADMIN: Tạo sự kiện mới kèm album ảnh
     */
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_EVENT')")
    public ResponseEntity<APIResponse<EventResponse>> createEvent(
            @RequestPart("data") EventRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        EventResponse event = eventService.createEvent(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<EventResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Successfully created event")
                .data(event)
                .build());
    }


    /**
     * ENDPOINT - ADMIN: Cập nhật sự kiện và quản lý album ảnh
     */
    @PutMapping(value = "/{id}", consumes =MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_EVENT')")
    public ResponseEntity<APIResponse<EventResponse>> updateEvent(
            @PathVariable Long id, 
            @RequestPart("data") EventRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        EventResponse event = eventService.updateEvent(id, request, images);
        return ResponseEntity.ok(APIResponse.<EventResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully updated event")
                .data(event)
                .build());
    }


    /**
     * ENDPOINT - ADMIN: Xóa mềm sự kiện
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_EVENT')")
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
     * ENDPOINT - ADMIN: Lấy tất cả sự kiện cho dashboard
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('MANAGE_EVENT')")
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
