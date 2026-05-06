package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.entity.Event;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.UserEventFollow;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.EventMapper;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserEventFollowRepository userEventFollowRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final EventMapper eventMapper;

    @Override
    public PageResponse<EventResponse> getAllEventsUser(String keyword, Long placeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("startTime").ascending());
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Page<Event> eventPage = eventRepository.searchEventsUser(keyword, placeId, now, pageable);

        List<EventResponse> data = eventPage.getContent().stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());

        return PageResponse.from(eventPage, data);
    }

    @Override
    @Transactional
    public void followEvent(EventFollowRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        UserEventFollow follow = UserEventFollow.builder()
                .user(user)
                .event(event)
                .notifyBeforeMinutes(request.getNotifyBeforeMinutes())
                .build();

        userEventFollowRepository.save(follow);
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Event event = eventMapper.toEvent(request);
        event.setPlace(place);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        eventMapper.updateEvent(event, request);
        event.setPlace(place);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        
        event.setDeleted(true);
        eventRepository.save(event);
    }

    @Override
    public PageResponse<EventResponse> getAllEventsAdmin(String keyword, Long placeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Event> eventPage = eventRepository.searchEventsAdmin(keyword, placeId, pageable);

        List<EventResponse> data = eventPage.getContent().stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());

        return PageResponse.from(eventPage, data);
    }
}
