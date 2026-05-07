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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
     * ENDPOINT - USER: Lấy danh sách sự kiện đang và sắp diễn ra (Phân trang)
     * @param keyword
     * @param placeId
     * @param page
     * @param size
     * @return
     */
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


    /**
     * ENDPOINT - ADMIN: Tạo sự kiện mới kèm album ảnh
     * @param request
     * @param images
     * @return
     */
    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request, org.springframework.web.multipart.MultipartFile[] images) {
        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Event event = eventMapper.toEvent(request);
        event.setPlace(place);
        event.setImages(new ArrayList<>());

        if (images != null && images.length > 0) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    try {
                        java.util.Map res = cloudinaryService.uploadFile(img);
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

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    /**
     * ENDPOINT - ADMIN: Cập nhật sự kiện và quản lý album ảnh
     * @param id
     * @param request
     * @param images
     * @return
     */
    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, MultipartFile[] images) {
        //  Tìm Event cần update
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

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

    /**
     * ENDPOINT - ADMIN: Lấy tất cả sự kiện (bao gồm đã xóa mềm) cho dashboard
     * @param keyword
     * @param placeId
     * @param page
     * @param size
     * @return
     */
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
