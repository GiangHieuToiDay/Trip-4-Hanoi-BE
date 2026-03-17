package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.entity.Event;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.UserEventFollow;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.EventMapper;
import com.trip4hanoi.app.repository.EventRepository;
import com.trip4hanoi.app.repository.UserEventFollowRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.EventService;
import lombok.RequiredArgsConstructor;
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
    private final EventMapper eventMapper;

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void followEvent(EventFollowRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        UserEventFollow follow = UserEventFollow.builder()
                .user(user)
                .event(event)
                .notifyBeforeMinutes(request.getNotifyBeforeMinutes())
                .build();

        userEventFollowRepository.save(follow);
    }
}
