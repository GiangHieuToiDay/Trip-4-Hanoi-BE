package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.EventFollowRequest;
import com.trip4hanoi.app.dto.req.EventRequest;
import com.trip4hanoi.app.dto.res.EventResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.EventMapper;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "EVENT-SERVICE")
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserEventFollowRepository userEventFollowRepository;
    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final EventMapper eventMapper;
    private final com.trip4hanoi.app.service.EventReminderService eventReminderService;
    private final com.trip4hanoi.app.service.CloudinaryService cloudinaryService;

    /**
     * Helper to get current userId from SecurityContext
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return null;
    }

    /**
     * ENDPOINT - USER: Lấy danh sách sự kiện đang và sắp diễn ra (Phân trang)
     */
    @Override
    public PageResponse<EventResponse> getAllEventsUser(String keyword, Long placeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("startTime").ascending());
        LocalDateTime now = LocalDateTime.now();
        Page<Event> eventPage = eventRepository.searchEventsUser(keyword, placeId, now, pageable);

        Long currentUserId = getCurrentUserId();

        List<EventResponse> data = eventPage.getContent().stream()
                .map(event -> {
                    EventResponse res = eventMapper.toEventResponse(event);
                    res.setFollowCount(userEventFollowRepository.countByEventId(event.getId()));
                    if (currentUserId != null) {
                        res.setIsFollowed(userEventFollowRepository.existsByUserIdAndEventId(currentUserId, event.getId()));
                    } else {
                        res.setIsFollowed(false);
                    }
                    return res;
                })
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

        // Kiểm tra xem đã follow chưa để tránh duplicate
        if (userEventFollowRepository.existsByUserIdAndEventId(userId, request.getEventId())) {
            return;
        }

        UserEventFollow follow = UserEventFollow.builder()
                .user(user)
                .event(event)
                .notifyBeforeMinutes(request.getNotifyBeforeMinutes())
                .build();

        userEventFollowRepository.save(follow);

        // Tạo reminder subscription nếu chưa có
        if (request.getNotifyBeforeMinutes() != null) {
            if (!eventSubscriptionRepository.existsByUserIdAndEventIdAndNotifyBeforeMinutes(
                    userId, request.getEventId(), request.getNotifyBeforeMinutes())) {
                EventSubscription sub = new EventSubscription();
                sub.setUserId(userId);
                sub.setEventId(request.getEventId());
                sub.setNotifyBeforeMinutes(request.getNotifyBeforeMinutes());
                sub.setNotified(false);
                eventSubscriptionRepository.save(sub);
            }
        }
    }


    @Override
    @Transactional
    public void unfollowEvent(Long eventId, Long userId) {
        userEventFollowRepository.deleteByUserIdAndEventId(userId, eventId);
    }

    /**
     * ENDPOINT - ADMIN: Tạo sự kiện mới kèm album ảnh
     */
    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request, MultipartFile[] images) {
        if (eventRepository.findByNameAndDeletedFalse(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.TITLE_EXIST);
        }

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Event event = eventMapper.toEvent(request);
        event.setPlace(place);
        event.setImages(new ArrayList<>());

        if (images != null && images.length > 0) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    try {
                        Map res = cloudinaryService.uploadFile(img);
                        event.getImages().add(EventImage.builder()
                                .imageUrl(res.get("secure_url").toString())
                                .publicId(res.get("public_id").toString())
                                .event(event)
                                .build());
                    } catch (Exception e) {
                       log.error(e.getMessage());
                    }
                }
            }
        }

        EventResponse response = eventMapper.toEventResponse(eventRepository.save(event));
        response.setFollowCount(0L);
        response.setIsFollowed(false);
        return response;
    }

    /**
     * ENDPOINT - ADMIN: Cập nhật sự kiện và quản lý album ảnh
     */
    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, MultipartFile[] images) {
        //  Tìm Event cần update
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Check name uniqueness if changed
        if (!event.getName().equals(request.getName())) {
            if (eventRepository.findByNameAndDeletedFalse(request.getName()).isPresent()) {
                throw new AppException(ErrorCode.TITLE_EXIST);
            }
        }

        //  Tìm Place mới (nếu có gửi placeId)
        if (request.getPlaceId() != null) {
            Place place = placeRepository.findById(request.getPlaceId())
                    .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));
            event.setPlace(place);
        }


        eventMapper.updateEvent(event, request);

        //  Xử lý album ảnh
        List<EventImage> currentImages = event.getImages();

        // ---  (VALIDATION) ID ẢNH ---
        List<Long> actualImageIds = currentImages.stream()
                .map(EventImage::getId)
                .collect(Collectors.toList());

        if (request.getKeepImageIds() != null) {
            for (Long keepId : request.getKeepImageIds()) {
                // Nếu gửi ID không tồn tại trong danh sách ảnh của Event này -> Báo lỗi
                if (!actualImageIds.contains(keepId)) {
                    throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
                }
            }
        }


        //  Xác định danh sách ảnh cần xóa khỏi Cloudinary và Database
        List<EventImage> toRemove = new ArrayList<>();
        if (request.getKeepImageIds() != null) {
            for (EventImage img : currentImages) {
                // Nếu ảnh hiện tại không nằm trong danh sách muốn giữ -> Xóa
                if (!request.getKeepImageIds().contains(img.getId())) {
                    toRemove.add(img);
                }
            }
        } else {
            // Nếu không gửi keepImageIds, mặc định xóa sạch ảnh cũ
            toRemove.addAll(currentImages);
        }

        // Thực hiện xóa
        for (EventImage img : toRemove) {
            cloudinaryService.deleteFile(img.getPublicId());
            currentImages.remove(img);
        }

        // Upload thêm ảnh mới (nếu có)
        if (images != null && images.length > 0) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    try {
                        Map res = cloudinaryService.uploadFile(img);
                        currentImages.add(EventImage.builder()
                                .imageUrl(res.get("secure_url").toString())
                                .publicId(res.get("public_id").toString())
                                .event(event)
                                .build());
                    } catch (Exception e) {
                        log.error("Upload image failed: " + e.getMessage());
                    }
                }
            }
        }

        Event savedEvent = eventRepository.save(event);
        EventResponse response = eventMapper.toEventResponse(savedEvent);
        response.setFollowCount(userEventFollowRepository.countByEventId(id));
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            response.setIsFollowed(userEventFollowRepository.existsByUserIdAndEventId(currentUserId, id));
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        
        event.setDeleted(true);
        eventRepository.save(event);
    }

    /**
     * ENDPOINT - ADMIN: Lấy tất cả sự kiện (bao gồm đã xóa mềm) cho dashboard
     */
    @Override
    public PageResponse<EventResponse> getAllAdmin(String keyword, Long placeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Event> eventPage = eventRepository.searchEventsAdmin(keyword, placeId, pageable);

        List<EventResponse> data = eventPage.getContent().stream()
                .map(event -> {
                    EventResponse res = eventMapper.toEventResponse(event);
                    res.setFollowCount(userEventFollowRepository.countByEventId(event.getId()));
                    return res;
                })
                .collect(Collectors.toList());

        return PageResponse.from(eventPage, data);
    }

    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        
        EventResponse response = eventMapper.toEventResponse(event);
        response.setFollowCount(userEventFollowRepository.countByEventId(id));
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            response.setIsFollowed(userEventFollowRepository.existsByUserIdAndEventId(currentUserId, id));
        } else {
            response.setIsFollowed(false);
        }
        
        return response;
    }

    @Override
    public List<EventResponse> getFollowedEvents(Long userId) {
        List<UserEventFollow> follows = userEventFollowRepository.findByUserId(userId);
        return follows.stream()
                .map(follow -> {
                    EventResponse res = eventMapper.toEventResponse(follow.getEvent());
                    res.setFollowCount(userEventFollowRepository.countByEventId(follow.getEvent().getId()));
                    res.setIsFollowed(true);
                    return res;
                })
                .collect(Collectors.toList());
    }
}
