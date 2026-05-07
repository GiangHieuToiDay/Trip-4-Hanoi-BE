package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.dto.res.PageResponse;


import org.springframework.web.multipart.MultipartFile;


import java.util.List;

public interface EventService {
    PageResponse<EventResponse> getAllEventsUser(String keyword, Long placeId, int page, int size);
    void followEvent(EventFollowRequest request, Long userId);
    EventResponse createEvent(EventRequest request, MultipartFile[] images);
    EventResponse updateEvent(Long id, EventRequest request, MultipartFile[] images);
    void deleteEvent(Long id);
    PageResponse<EventResponse> getAllEventsAdmin(String keyword, Long placeId, int page, int size);
}
