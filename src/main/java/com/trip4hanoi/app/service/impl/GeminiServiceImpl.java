package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.entity.Event;
import com.trip4hanoi.app.repository.EventRepository;
import tools.jackson.databind.ObjectMapper;
import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.dto.res.ScheduleItem;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.service.RecommendationService;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.repository.UserPreferenceRepository;
import com.trip4hanoi.app.repository.UserLocationHistoryRepository;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GEMINI SERVICE")
public class  GeminiServiceImpl implements GeminiService {

    private final WebClient geminiWebClient;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserLocationHistoryRepository locationHistoryRepository;
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public ChatResponse chatWithAI(String userMessage, Long userId) {
        long startTime = System.currentTimeMillis();

        //  Chạy song song các tác vụ lấy dữ liệu (Parallel Fetching)
        CompletableFuture<List<PlaceResponse>> recommendedPlacesFuture = CompletableFuture.supplyAsync(() -> 
            recommendationService.getPersonalizedRecommendations(15)); // Giảm xuống 15 điểm tiêu biểu

        CompletableFuture<String> userContextFuture = CompletableFuture.supplyAsync(() -> {
            if (userId == null || userId == 0L) return "Khách vãng lai";
            var user = userRepository.findById(userId).orElse(null);
            var prefs = userPreferenceRepository.findByUserId(userId).stream()
                    .map(p -> p.getCategory().getName())
                    .collect(Collectors.joining(", "));
            var topDistricts = locationHistoryRepository.findTopDistricts(userId, LocalDateTime.now().minusDays(15));
            
            return String.format("User: %s, Gu: %s, Khu vực hay ở: %s",
                    user != null ? user.getUsername() : "Khách",
                    prefs.isEmpty() ? "Tổng hợp" : prefs,
                    topDistricts.isEmpty() ? "Hà Nội" : String.join(", ", topDistricts));
        });

        // Đợi dữ liệu sẵn sàng (Time-efficient)
        List<PlaceResponse> recommendedPlaces = recommendedPlacesFuture.join();
        String userContext = userContextFuture.join();

        // Chỉ lấy sự kiện liên quan và đang diễn ra (Data Minimization)
        Set<Long> placeIds = recommendedPlaces.stream().map(PlaceResponse::getId).collect(Collectors.toSet());
        String eventsContext = eventRepository.findAll().stream()
                .filter(e -> placeIds.contains(e.getPlace().getId()))
                .filter(e -> !LocalDateTime.now().isBefore(e.getStartTime()) && !LocalDateTime.now().isAfter(e.getEndTime()))
                .map(e -> String.format("- Sự kiện: %s tại [ID:%d]. Mô tả: %s", e.getName(), e.getPlace().getId(), e.getDescription()))
                .collect(Collectors.joining("\n"));

        //  Prompt Compression (Chỉ gửi thông tin cốt lõi)
        String placesPrompt = recommendedPlaces.stream()
                .map(p -> String.format("[%d]%s(%s):%s.Gu:%b", 
                        p.getId(), p.getName(), p.getDistrict(), p.getDescription(), p.getIsRecommended()))
                .collect(Collectors.joining("|"));

        String prompt = String.format(
                "Hệ thống: Bạn là bạn bản địa Hà Nội tư vấn lịch trình. CHỈ TRẢ VỀ JSON KHÔNG CÓ MARKDOWN. " +
                "Bối cảnh: %s. Địa điểm gợi ý: %s. Sự kiện: %s. " +
                "Người dùng hỏi: \"%s\". " +
                "Nhiệm vụ: Trả về JSON duy nhất. Ưu tiên địa điểm có Gu:true và có Sự kiện. Lời thoại thân thiện. " +
                "Lưu ý: placeId TRONG timeline PHẢI là số Long và thuộc danh sách gợi ý phía trên. " +
                "Cấu trúc JSON: {introduction, timeline:[{time, activity, placeId, note, estimatedCost}], summary, suggestedPlaceIds:[]}",
                userContext, placesPrompt, eventsContext, userMessage
        );

        log.info("Data fetching & preparation took: {} ms", System.currentTimeMillis() - startTime);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            String finalUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;
            
            Map response = geminiWebClient.post()
                    .uri(finalUrl)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("candidates")) {
                throw new RuntimeException("AI response error");
            }

            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            String aiText = (String) firstPart.get("text");

            // Xử lý JSON từ AI
            int start = aiText.indexOf("{");
            int end = aiText.lastIndexOf("}");
            if (start != -1 && end != -1) {
                String cleanJson = aiText.substring(start, end + 1);
                return mapper.readValue(cleanJson, ChatResponse.class);
            }
            
            return ChatResponse.builder().introduction(aiText).timeline(new ArrayList<>()).summary("").suggestedPlaceIds(new ArrayList<>()).build();

        } catch (Exception e) {
            log.error(">>> AI ERROR: ", e);
            return ChatResponse.builder().introduction("Xin lỗi, tôi đang xử lý hơi chậm. Bạn thử lại nhé!").timeline(new ArrayList<>()).build();
        }
    }
}
