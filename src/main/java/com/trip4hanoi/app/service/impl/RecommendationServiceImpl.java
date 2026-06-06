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
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "personalized_recommendations", 
               key =  "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getPrincipal() instanceof T(org.springframework.security.oauth2.jwt.Jwt) ? " +
                       "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getPrincipal().getClaims().get('id') + '_' + #limit : 'guest_' + #limit")
    public List<PlaceResponse> getPersonalizedRecommendations(int limit) {
        Long userId = getCurrentUserId();
        
        // Lấy tất cả địa điểm (Eager loaded category/images via EntityGraph)
        List<Place> allPlaces = placeRepository.findAllByDeletedFalse();

        //  Lấy ID các địa điểm có sự kiện đang diễn ra (Tránh N+1)
        LocalDateTime now = LocalDateTime.now();
        Set<Long> placeIdsWithEvents = eventRepository.findAll().stream()
                .filter(e -> !e.isDeleted() && e.getPlace() != null && !now.isBefore(e.getStartTime()) && !now.isAfter(e.getEndTime()))
                .map(e -> e.getPlace().getId())
                .collect(Collectors.toSet());

        if (userId == null || userId == 0L) {
            return allPlaces.stream()
                    .sorted(Comparator.comparing(Place::getRatingAvg).reversed())
                    .limit(limit)
                    .map(place -> {
                        PlaceResponse res = placeMapper.toPlaceResponse(place);
                        res.setHasActiveEvent(placeIdsWithEvents.contains(place.getId()));
                        return res;
                    })
                    .collect(Collectors.toList());
        }

        //  Thông tin người dùng
        List<String> topDistricts = locationHistoryRepository.findTopDistricts(userId, LocalDateTime.now().minusDays(15));
        Set<Long> preferredCategoryIds = userPreferenceRepository.findByUserId(userId).stream()
                .filter(up -> up.getCategory() != null)
                .map(up -> up.getCategory().getId())
                .collect(Collectors.toSet());

        //  Thuật toán tính điểm (Scoring)
        return allPlaces.stream()
                .map(place -> {
                    double score = calculateScore(place, topDistricts, preferredCategoryIds, placeIdsWithEvents);
                    return new PlaceScore(place, score);
                })
                .sorted(Comparator.comparing(PlaceScore::getScore).reversed())
                .limit(limit)
                .map(ps -> {
                    PlaceResponse res = placeMapper.toPlaceResponse(ps.getPlace());
                    if (ps.getPlace().getCategory() != null && preferredCategoryIds.contains(ps.getPlace().getCategory().getId())) {
                        res.setIsRecommended(true);
                    }
                    res.setHasActiveEvent(placeIdsWithEvents.contains(ps.getPlace().getId()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    private double calculateScore(Place place, List<String> topDistricts, Set<Long> preferredCategoryIds, Set<Long> placeIdsWithEvents) {
        double score = place.getRatingAvg() != null ? place.getRatingAvg() : 0.0;

        // Cộng 2 điểm nếu ở Quận hay đi (Hot Zone)
        if (!topDistricts.isEmpty() && topDistricts.get(0) != null && topDistricts.get(0).equalsIgnoreCase(place.getDistrict())) {
            score += 2.0;
        } else if (place.getDistrict() != null && topDistricts.contains(place.getDistrict())) {
            score += 1.0;
        }

        // Cộng 1.5 điểm nếu thuộc Category yêu thích
        if (place.getCategory() != null && preferredCategoryIds.contains(place.getCategory().getId())) {
            score += 1.5;
        }

        // Ưu tiên quán có nhiều view
        if (place.getViewCount() != null) {
            score += (place.getViewCount() * 0.01);
        }

        // [MỚI] Ưu tiên cực cao nếu có Sự kiện đang diễn ra
        if (placeIdsWithEvents.contains(place.getId())) {
            score += 5.0;
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
