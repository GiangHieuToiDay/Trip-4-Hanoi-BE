package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import java.util.List;

public interface EventService {
    List<EventResponse> getAllEvents();
    void followEvent(EventFollowRequest request, Long userId);
}
