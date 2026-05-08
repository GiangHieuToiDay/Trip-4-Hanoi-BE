package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.mapper.PlaceMapper;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.UserLocationHistoryRepository;
import com.trip4hanoi.app.repository.UserPreferenceRepository;
import com.trip4hanoi.app.repository.EventRepository;
import com.trip4hanoi.app.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RECOMMENDATION-SERVICE")
public class RecommendationServiceImpl implements RecommendationService {

    private final UserLocationHistoryRepository locationHistoryRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;
    private final EventRepository eventRepository;

    @Override
    public List<PlaceResponse> getPersonalizedRecommendations(int limit) {
        Long userId = getCurrentUserId();
        if (userId == 0L) {
            // Nếu khách vãng lai, gợi ý top rating chung
            return placeRepository.findAllByDeletedFalse().stream()
                    .sorted(Comparator.comparing(Place::getRatingAvg).reversed())
                    .limit(limit)
                    .map(placeMapper::toPlaceResponse)
                    .collect(Collectors.toList());
        }

        //  Lấy top các Quận người dùng hay ghé qua trong 15 ngày
        List<String> topDistricts = locationHistoryRepository.findTopDistricts(userId, LocalDateTime.now().minusDays(15));
        
        // Lấy danh mục yêu thích
        Set<Long> preferredCategoryIds = userPreferenceRepository.findByUserId(userId).stream()
                .map(up -> up.getCategory().getId())
                .collect(Collectors.toSet());

        //  Lấy tất cả địa điểm chưa bị xóa
        List<Place> allPlaces = placeRepository.findAllByDeletedFalse();

        //  Thuật toán tính điểm (Scoring)
        return allPlaces.stream()
                .map(place -> {
                    double score = calculateScore(place, topDistricts, preferredCategoryIds);
                    return new PlaceScore(place, score);
                })
                .sorted(Comparator.comparing(PlaceScore::getScore).reversed())
                .limit(limit)
                .map(ps -> {
                    PlaceResponse res = placeMapper.toPlaceResponse(ps.getPlace());
                    // Đánh dấu logic bổ sung (nếu cần)
                    if (preferredCategoryIds.contains(ps.getPlace().getCategory().getId())) {
                        res.setIsRecommended(true);
                    }
                    
                    // [MỚI] Đánh dấu nếu có event
                    res.setHasActiveEvent(eventRepository.findByPlaceId(ps.getPlace().getId()).stream()
                            .anyMatch(e -> !LocalDateTime.now().isBefore(e.getStartTime()) && !LocalDateTime.now().isAfter(e.getEndTime())));
                    
                    return res;
                })
                .collect(Collectors.toList());
    }

    private double calculateScore(Place place, List<String> topDistricts, Set<Long> preferredCategoryIds) {
        double score = place.getRatingAvg() != null ? place.getRatingAvg() : 0.0;

        // Cộng 2 điểm nếu ở Quận hay đi (Hot Zone)
        if (!topDistricts.isEmpty() && topDistricts.get(0).equalsIgnoreCase(place.getDistrict())) {
            score += 2.0;
        } else if (topDistricts.contains(place.getDistrict())) {
            score += 1.0;
        }

        // Cộng 1.5 điểm nếu thuộc Category yêu thích
        if (preferredCategoryIds.contains(place.getCategory().getId())) {
            score += 1.5;
        }

        // Ưu tiên quán có nhiều view
        score += (place.getViewCount() * 0.01);

        // [MỚI] Ưu tiên cực cao nếu có Sự kiện đang diễn ra
        boolean hasActiveEvent = eventRepository.findByPlaceId(place.getId()).stream()
                .anyMatch(e -> !LocalDateTime.now().isBefore(e.getStartTime()) && !LocalDateTime.now().isAfter(e.getEndTime()));
        if (hasActiveEvent) {
            score += 5.0; // Điểm thưởng lớn nhất để đẩy Event lên top
        }

        return score;
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }

    @lombok.Value
    private static class PlaceScore {
        Place place;
        double score;
    }
}
