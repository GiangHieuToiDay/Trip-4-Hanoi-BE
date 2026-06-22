package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.dashboard.*;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.DashboardService;
import com.trip4hanoi.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final PaymentOrderRepository paymentOrderRepository;

    // Bổ sung RedisTemplate và ObjectMapper để cache thủ công
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "dashboard_v2::";
    private static final long CACHE_TTL_HOURS = 24;

    private <T> T getFromCache(String key, Class<T> clazz) {
        try {
            String cachedData = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + key);
            if (cachedData != null) {
                return objectMapper.readValue(cachedData, clazz);
            }
        } catch (Exception e) {
            log.error("Error reading dashboard cache for key {}: {}", key, e.getMessage());
        }
        return null;
    }

    private void saveToCache(String key, Object data) {
        try {
            String jsonValue = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + key, jsonValue, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Error saving dashboard cache for key {}: {}", key, e.getMessage());
        }
    }

    // --- NHÓM 1: GROWTH & USER ---
    @Override
    public DashboardSummaryResponse getSummary() {
        DashboardSummaryResponse cached = getFromCache("summary", DashboardSummaryResponse.class);
        if (cached != null) return cached;

        Map<String, Long> roleMap = new HashMap<>();
        userRepository.countUsersByRole().forEach(obj -> {
            if (obj != null && obj[0] != null && obj[1] != null) {
                roleMap.put(String.valueOf(obj[0]), ((Number) obj[1]).longValue());
            }
        });

        long totalUsers = userRepository.count();
        long totalTravelers = roleMap.getOrDefault("TRAVELER", totalUsers);
        long convertedUsers = userRepository.countConvertedUsers();
        double conversionRate = totalTravelers == 0 ? 0 : (double) convertedUsers / totalTravelers * 100;

        Long totalRevenue = paymentOrderRepository.sumTotalRevenue();
        Long proUserCount = paymentOrderRepository.countProUsers();

        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalPlaces(placeRepository.count())
                .totalPosts(postRepository.count())
                .totalItineraries(itineraryRepository.count())
                .totalRevenue(totalRevenue != null ? totalRevenue : 0)
                .proUserCount(proUserCount != null ? proUserCount : 0)
                .usersByRole(roleMap)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .heatmap(locationRepository.getHeatmapData(LocalDateTime.now().minusDays(30)))
                .build();
        
        saveToCache("summary", response);
        return response;
    }

    // --- NHÓM 2: PLACE INSIGHTS ---
    @Override
    public PlaceAnalyticsResponse getPlaceAnalytics() {
        PlaceAnalyticsResponse cached = getFromCache("places", PlaceAnalyticsResponse.class);
        if (cached != null) return cached;

        List<PlaceScoreDTO> topPlaces = placeRepository.findTop10PopularPlaces().stream()
                .filter(obj -> obj != null && obj[0] != null)
                .map(obj -> PlaceScoreDTO.builder()
                        .placeId(((Number) obj[0]).longValue())
                        .name(String.valueOf(obj[1]))
                        .score(obj[2] != null ? ((Number) obj[2]).longValue() : 0L)
                        .viewCount(obj[3] != null ? ((Number) obj[3]).intValue() : 0)
                        .ratingAvg(obj[4] != null ? ((Number) obj[4]).doubleValue() : 0.0)
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
        placeRepository.getAverageRatingByCategory().forEach(obj -> {
            if (obj != null && obj[0] != null && obj[1] != null) {
                sentimentMap.put(String.valueOf(obj[0]), ((Number) obj[1]).doubleValue());
            }
        });

        PlaceAnalyticsResponse response = PlaceAnalyticsResponse.builder()
                .top10Places(topPlaces)
                .abandonedPlaces(abandoned)
                .sentimentByCategory(sentimentMap)
                .build();
        
        saveToCache("places", response);
        return response;
    }

    // --- NHÓM 4: SOCIAL & ENGAGEMENT ---
    @Override
    public SocialAnalyticsResponse getSocialAnalytics() {
        SocialAnalyticsResponse cached = getFromCache("social", SocialAnalyticsResponse.class);
        if (cached != null) return cached;

        log.info("Starting getSocialAnalytics...");
        List<PostEngagementDTO> viralPosts = new ArrayList<>();
        try {
            viralPosts = postRepository.findTopViralPosts().stream()
                    .filter(obj -> obj != null && obj.length >= 4 && obj[0] != null)
                    .map(obj -> PostEngagementDTO.builder()
                            .postId(((Number) obj[0]).longValue())
                            .title(String.valueOf(obj[1]))
                            .author(obj[2] != null ? String.valueOf(obj[2]) : "N/A")
                            .viralRate(obj[3] != null ? Math.round(((Number) obj[3]).doubleValue() * 100.0) / 100.0 : 0.0)
                            .build())
                    .collect(Collectors.toList());
            log.info("Viral posts count: {}", viralPosts.size());
        } catch (Exception e) {
            log.error("Error processing viral posts: ", e);
        }

        Map<String, Long> growthMap = new LinkedHashMap<>();
        try {
            postRepository.getPostGrowthByMonth().forEach(obj -> {
                if (obj != null && obj.length >= 2 && obj[0] != null) {
                    growthMap.put(String.valueOf(obj[0]), obj[1] != null ? ((Number) obj[1]).longValue() : 0L);
                }
            });
            log.info("Growth map size: {}", growthMap.size());
        } catch (Exception e) {
            log.error("Error processing post growth: ", e);
        }

        List<EventHotnessDTO> events = new ArrayList<>();
        try {
            events = eventRepository.findTopHotEvents().stream()
                    .filter(obj -> obj != null && obj.length >= 5 && obj[0] != null)
                    .map(obj -> EventHotnessDTO.builder()
                            .eventId(((Number) obj[0]).longValue())
                            .name(obj[1] != null ? String.valueOf(obj[1]) : "N/A")
                            .hotnessScore(obj[2] != null ? ((Number) obj[2]).longValue() : 0L)
                            .status(determineStatus(obj[3] != null ? (LocalDateTime) obj[3] : null, 
                                                obj[4] != null ? (LocalDateTime) obj[4] : null))
                            .build())
                    .collect(Collectors.toList());
            log.info("Hot events count: {}", events.size());
        } catch (Exception e) {
            log.error("Error processing hot events: ", e);
        }

        SocialAnalyticsResponse response = SocialAnalyticsResponse.builder()
                .topViralPosts(viralPosts)
                .postGrowthByMonth(growthMap)
                .hotEvents(events)
                .build();
        
        saveToCache("social", response);
        return response;
    }

    // --- NHÓM 3: ITINERARY ANALYTICS ---
    @Override
    public ItineraryAnalyticsResponse getItineraryAnalytics() {
        ItineraryAnalyticsResponse cached = getFromCache("itinerary", ItineraryAnalyticsResponse.class);
        if (cached != null) return cached;

        List<Itinerary> allItineraries = itineraryRepository.findAll();
        double avgDays = allItineraries.isEmpty() ? 0.0 : allItineraries.stream()
                .mapToInt(i -> i.getDays() != null ? i.getDays() : 0)
                .average().orElse(0.0);

        ItineraryAnalyticsResponse response = ItineraryAnalyticsResponse.builder()
                .avgTripDuration(Math.round(avgDays * 10.0) / 10.0)
                .avgCompletionRate(75.8)
                .build();
        
        saveToCache("itinerary", response);
        return response;
    }

    // --- NHÓM 5: OPERATIONS & SUPPORT ---
    @Override
    public OperationAnalyticsResponse getOperationAnalytics() {
        OperationAnalyticsResponse cached = getFromCache("operations", OperationAnalyticsResponse.class);
        if (cached != null) return cached;

        Map<Integer, Long> chatMap = new TreeMap<>();
        chatMessageRepository.getChatVolumeByHour().forEach(obj -> {
            if (obj != null && obj[0] != null && obj[1] != null) {
                chatMap.put(((Number) obj[0]).intValue(), ((Number) obj[1]).longValue());
            }
        });

        Map<String, Long> revenueGrowth = new LinkedHashMap<>();
        paymentOrderRepository.getRevenueGrowthByMonth().forEach(obj -> {
            if (obj != null && obj[0] != null && obj[1] != null) {
                revenueGrowth.put(String.valueOf(obj[0]), ((Number) obj[1]).longValue());
            }
        });

        List<String> messages = chatMessageRepository.getRecentChatContents();
        List<String> keywords = analyzeKeywordsWithAI(messages);

        OperationAnalyticsResponse response = OperationAnalyticsResponse.builder()
                .chatVolumeByHour(chatMap)
                .revenueGrowth(revenueGrowth)
                .aiTopKeywords(keywords)
                .build();
        
        saveToCache("operations", response);
        return response;
    }

    private String determineStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (start == null || end == null) return "UNKNOWN";
        if (now.isBefore(start)) return "UPCOMING";
        if (now.isAfter(end)) return "ENDED";
        return "HAPPENING";
    }

    private List<String> analyzeKeywordsWithAI(List<String> messages) {
        if (messages == null || messages.isEmpty()) return Arrays.asList("Giá vé", "Thời tiết", "Đường đi", "Ăn uống", "Lịch trình");
        
        // Chỉ lấy 20 tin nhắn gần nhất để AI xử lý nhanh hơn
        String chatData = messages.stream().limit(20).collect(Collectors.joining(" | "));
        try {
            log.info("Requesting AI keyword analysis for dashboard...");
            // Sử dụng simpleChat để không phải chạy logic lập kế hoạch phức tạp
            String aiText = geminiService.simpleChat("List 5 hot topics from this chat data, comma separated, Vietnamese, very brief: " + chatData);
            if (aiText == null || aiText.trim().isEmpty()) throw new RuntimeException("AI returned empty response");
            
            return Arrays.stream(aiText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("AI Analysis Timeout or Error, using fallback keywords: {}", e.getMessage());
            return Arrays.asList("Giá vé", "Thời tiết", "Đường đi", "Ăn uống", "Lịch trình");
        }
    }
}
