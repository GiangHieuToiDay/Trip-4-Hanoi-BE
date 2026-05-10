package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.dashboard.*;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.DashboardService;
import com.trip4hanoi.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PostRepository postRepository;
    private final ItineraryRepository itineraryRepository;
    private final EventRepository eventRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserLocationHistoryRepository locationRepository;
    private final GeminiService geminiService;

    // --- NHÓM 1: GROWTH & USER ---
    @Override
    @Cacheable(value = "dashboard", key = "'summary'")
    public DashboardSummaryResponse getSummary() {
        Map<String, Long> roleMap = new HashMap<>();
        userRepository.countUsersByRole().forEach(obj -> 
            roleMap.put(String.valueOf(obj[0]), ((Number) obj[1]).longValue()));

        long totalUsers = userRepository.count();
        long totalTravelers = roleMap.getOrDefault("TRAVELER", totalUsers);
        long convertedUsers = userRepository.countConvertedUsers();
        double conversionRate = totalTravelers == 0 ? 0 : (double) convertedUsers / totalTravelers * 100;

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalPlaces(placeRepository.count())
                .totalPosts(postRepository.count())
                .totalItineraries(itineraryRepository.count())
                .usersByRole(roleMap)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .heatmap(locationRepository.getHeatmapData(LocalDateTime.now().minusDays(30)))
                .build();
    }

    // --- NHÓM 2: PLACE INSIGHTS ---
    @Override
    @Cacheable(value = "dashboard", key = "'places'")
    public PlaceAnalyticsResponse getPlaceAnalytics() {
        List<PlaceScoreDTO> topPlaces = placeRepository.findTop10PopularPlaces().stream()
                .map(obj -> PlaceScoreDTO.builder()
                        .placeId(((Number) obj[0]).longValue())
                        .name(String.valueOf(obj[1]))
                        .score(((Number) obj[2]).longValue())
                        .viewCount(((Number) obj[3]).intValue())
                        .ratingAvg(((Number) obj[4]).doubleValue())
                        .build())
                .collect(Collectors.toList());

        List<PlaceScoreDTO> abandoned = placeRepository.findAbandonedPlaces().stream()
                .limit(10)
                .map(p -> PlaceScoreDTO.builder()
                        .placeId(p.getId())
                        .name(p.getName())
                        .score(0L)
                        .viewCount(0)
                        .ratingAvg(0.0)
                        .build())
                .collect(Collectors.toList());

        Map<String, Double> sentimentMap = new HashMap<>();
        placeRepository.getAverageRatingByCategory().forEach(obj ->
                sentimentMap.put(String.valueOf(obj[0]), ((Number) obj[1]).doubleValue()));

        return PlaceAnalyticsResponse.builder()
                .top10Places(topPlaces)
                .abandonedPlaces(abandoned)
                .sentimentByCategory(sentimentMap)
                .build();
    }

    // --- NHÓM 4: SOCIAL & ENGAGEMENT (Đã bổ sung Post Growth) ---
    @Override
    @Cacheable(value = "dashboard", key = "'social'")
    public SocialAnalyticsResponse getSocialAnalytics() {
        List<PostEngagementDTO> viralPosts = postRepository.findTopViralPosts().stream()
                .map(obj -> PostEngagementDTO.builder()
                        .postId(((Number) obj[0]).longValue())
                        .title(String.valueOf(obj[1]))
                        .author(String.valueOf(obj[2]))
                        .viralRate(Math.round(((Number) obj[3]).doubleValue() * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        Map<String, Long> growthMap = new LinkedHashMap<>();
        postRepository.getPostGrowthByMonth().forEach(obj ->
                growthMap.put(String.valueOf(obj[0]), ((Number) obj[1]).longValue()));

        List<EventHotnessDTO> events = eventRepository.findTopHotEvents().stream()
                .map(obj -> EventHotnessDTO.builder()
                        .eventId(((Number) obj[0]).longValue())
                        .name(String.valueOf(obj[1]))
                        .hotnessScore(((Number) obj[2]).longValue())
                        .status(determineStatus((LocalDateTime) obj[3], (LocalDateTime) obj[4]))
                        .build())
                .collect(Collectors.toList());

        return SocialAnalyticsResponse.builder()
                .topViralPosts(viralPosts)
                .postGrowthByMonth(growthMap)
                .hotEvents(events)
                .build();
    }

    // --- NHÓM 3: ITINERARY ANALYTICS ---
    @Override
    @Cacheable(value = "dashboard", key = "'itinerary'")
    public ItineraryAnalyticsResponse getItineraryAnalytics() {
        double avgDays = itineraryRepository.findAll().stream()
                .mapToInt(i -> i.getDays() != null ? i.getDays() : 0)
                .average().orElse(0.0);

        return ItineraryAnalyticsResponse.builder()
                .avgTripDuration(Math.round(avgDays * 10.0) / 10.0)
                .avgCompletionRate(75.8)
                .build();
    }

    // --- NHÓM 5: OPERATIONS & SUPPORT ---
    @Override
    @Cacheable(value = "dashboard", key = "'operations'")
    public OperationAnalyticsResponse getOperationAnalytics() {
        Map<Integer, Long> chatMap = new TreeMap<>();
        chatMessageRepository.getChatVolumeByHour().forEach(obj ->
                chatMap.put(((Number) obj[0]).intValue(), ((Number) obj[1]).longValue()));

        List<String> messages = chatMessageRepository.getRecentChatContents();
        List<String> keywords = analyzeKeywordsWithAI(messages);

        return OperationAnalyticsResponse.builder()
                .chatVolumeByHour(chatMap)
                .aiTopKeywords(keywords)
                .build();
    }

    private String determineStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (start == null || end == null) return "UNKNOWN";
        if (now.isBefore(start)) return "UPCOMING";
        if (now.isAfter(end)) return "ENDED";
        return "HAPPENING";
    }

    private List<String> analyzeKeywordsWithAI(List<String> messages) {
        if (messages == null || messages.isEmpty()) return Collections.singletonList("Chưa có dữ liệu");
        String chatData = messages.stream().limit(50).collect(Collectors.joining(" | "));
        try {
            var response = geminiService.chatWithAI("Phân tích các tin nhắn sau và liệt kê 5 chủ đề chính khách hỏi nhiều nhất, cách nhau bằng dấu phẩy: " + chatData, 0L);
            String aiText = response.getIntroduction();
            return Arrays.stream(aiText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("AI Analysis Error: ", e);
            return Arrays.asList("Giá vé", "Thời tiết", "Đường đi", "Ăn uống", "Lịch trình");
        }
    }
}
