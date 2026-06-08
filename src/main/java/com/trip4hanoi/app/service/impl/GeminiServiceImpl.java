package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.repository.EventRepository;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.UserLocationHistoryRepository;
import com.trip4hanoi.app.repository.UserPreferenceRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.GeminiService;
import com.trip4hanoi.app.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GEMINI SERVICE")
public class GeminiServiceImpl implements GeminiService {

    private final WebClient geminiWebClient;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserLocationHistoryRepository locationHistoryRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKeysString;

    private List<String> apiKeys;
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        if (apiKeysString != null && !apiKeysString.isEmpty()) {
            apiKeys = java.util.Arrays.stream(apiKeysString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            apiKeys = new ArrayList<>();
        }
        log.info(">>> Loaded {} Gemini API Keys", apiKeys.size());
    }

    private String getCurrentKey() {
        if (apiKeys.isEmpty()) return "";
        return apiKeys.get(currentKeyIndex.get() % apiKeys.size());
    }

    private void rotateKey() {
        if (apiKeys.size() > 1) {
            int newIndex = currentKeyIndex.incrementAndGet();
            log.warn(">>> Hết Quota! Tự động chuyển sang API Key dự phòng (Index: {})", newIndex % apiKeys.size());
        }
    }

    @Override
    public ChatResponse chatWithAI(String userMessage, Long userId) {
        String currentKey = getCurrentKey();
        if (currentKey.length() > 8) {
            String maskedKey = currentKey.substring(0, 4) + "..." + currentKey.substring(currentKey.length() - 4);
            log.info(">>> Using Gemini API Key: {}", maskedKey);
        } else {
            log.error(">>> Gemini API Key is MISSING or too short!");
        }

        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        String currentDayOfWeek = now.getDayOfWeek().toString();
        String currentTimeStr = now.toLocalTime().toString().substring(0, 5);

        // Phân tích ý định sơ bộ để lấy dữ liệu phù hợp
        String lowerMsg = userMessage.toLowerCase();
        boolean needsHomestay = lowerMsg.contains("homestay") || lowerMsg.contains("nghỉ") || lowerMsg.contains("chỗ ở") || lowerMsg.contains("khách sạn");
        boolean needsRomantic = lowerMsg.contains("người yêu") || lowerMsg.contains("hẹn hò") || lowerMsg.contains("lãng mạn");

        // Chạy song song các tác vụ lấy dữ liệu (Parallel Fetching)
        CompletableFuture<List<PlaceResponse>> recommendedPlacesFuture = CompletableFuture.supplyAsync(() -> {
            List<PlaceResponse> places = new ArrayList<>();
            
            // 1. Lấy gợi ý cá nhân hóa (15 địa điểm)
            List<?> rawList = recommendationService.getPersonalizedRecommendations(15);
            places.addAll(rawList.stream()
                    .map(item -> objectMapper.convertValue(item, PlaceResponse.class))
                    .collect(Collectors.toList()));

            // 2. Nếu cần Homestay, chủ động lấy thêm 5 Homestay xịn
            if (needsHomestay) {
                placeRepository.findAllByDeletedFalse().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase("Homestay"))
                    .limit(5)
                    .forEach(p -> {
                        PlaceResponse pr = PlaceResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .district(p.getDistrict())
                                .description(p.getDescription())
                                .priceAvg(p.getPriceAvg())
                                .isRecommended(true)
                                .build();
                        if (places.stream().noneMatch(existing -> existing.getId().equals(pr.getId()))) {
                            places.add(pr);
                        }
                    });
            }
            
            // 3. Nếu là cặp đôi, lấy thêm các chỗ chill/lãng mạn
            if (needsRomantic) {
                // Ưu tiên các category: Cafe, Nhà hàng, Hồ, Công viên
                // (Logic này có thể mở rộng thêm filter cụ thể)
            }

            return places;
        });

        CompletableFuture<String> userContextFuture = CompletableFuture.supplyAsync(() -> {
            if (userId == null || userId == 0L) return "Khách vãng lai";
            var user = userRepository.findById(userId).orElse(null);
            var prefs = userPreferenceRepository.findByUserId(userId).stream()
                    .map(up -> up.getCategory().getName())
                    .collect(Collectors.joining(", "));
            var topDistricts = locationHistoryRepository.findTopDistricts(userId, LocalDateTime.now().minusDays(15));
            
            return String.format("User: %s, Gu: %s, Khu vực hay ở: %s. Thời gian hiện tại: %s, %s.",
                    user != null ? user.getUsername() : "Khách",
                    prefs.isEmpty() ? "Tổng hợp" : prefs,
                    topDistricts.isEmpty() ? "Hà Nội" : String.join(", ", topDistricts),
                    currentDayOfWeek, currentTimeStr);
        });

        // Đợi dữ liệu sẵn sàng
        List<PlaceResponse> recommendedPlaces = recommendedPlacesFuture.join();
        String userContext = userContextFuture.join();

        // Chỉ lấy sự kiện liên quan và đang diễn ra
        Set<Long> placeIds = recommendedPlaces.stream().map(PlaceResponse::getId).collect(Collectors.toSet());
        String eventsContext = eventRepository.findAll().stream()
                .filter(e -> placeIds.contains(e.getPlace().getId()))
                .filter(e -> !LocalDateTime.now().isBefore(e.getStartTime()) && !LocalDateTime.now().isAfter(e.getEndTime()))
                .map(e -> String.format("- Sự kiện: %s tại [ID:%d]. Mô tả: %s", e.getName(), e.getPlace().getId(), e.getDescription()))
                .collect(Collectors.joining("\n"));

        // Prompt Compression
        String placesPrompt = recommendedPlaces.stream()
                .map(p -> String.format("[%d]%s(%s):%s.Giá:%d.Gu:%b", 
                        p.getId(), p.getName(), p.getDistrict(), p.getDescription(), p.getPriceAvg(), p.getIsRecommended()))
                .collect(Collectors.joining("|"));

        String prompt = String.format(
                "Hệ thống: Bạn là 'Local Buddy' - một người bạn bản địa Hà Nội am hiểu và thông minh. CHỈ TRẢ VỀ JSON KHÔNG CÓ MARKDOWN.\n" +
                "Nhiệm vụ & Quy tắc cứng:\n" +
                "1. PHÂN LOẠI Ý ĐỊNH:\n" +
                "   - Nếu người dùng chào hỏi, tán gẫu hoặc hỏi chung chung: Đặt timeline là [] và trả lời thân thiện trong 'introduction'.\n" +
                "   - Nếu người dùng yêu cầu lịch trình: Thực hiện lên lịch trình chi tiết trong 'timeline'.\n" +
                "2. NHẬN THỨC THỜI GIAN THỰC: Bối cảnh thời gian hiện tại là %s. Nếu khách hỏi 'ngay bây giờ' đi đâu ăn gì, phải ưu tiên món ăn phù hợp (11h-13h: Ăn trưa, 18h-20h: Ăn tối).\n" +
                "3. TUYỆT ĐỐI BẢO MẬT ID: Không bao giờ hiển thị các con số ID địa điểm (ví dụ: 'ID 1', '[1]', 'địa điểm 5') trong nội dung văn bản (introduction và summary). Người dùng không được thấy các ID này. Các ID chỉ dùng ngầm trong 'placeId' và 'suggestedPlaceIds'.\n" +
                "4. LOGIC THỰC TẾ: Không xếp Đền/Chùa/Bảo tàng sau 18h. Không đi Cinema/Bar buổi sáng. Nếu đi cặp đôi, hãy gợi ý Homestay nếu muộn hoặc khách yêu cầu nghỉ ngơi.\n" +
                "5. DỮ LIỆU: Sử dụng danh sách địa điểm khả dụng: %s.\n" +
                "6. PERSONA: Ngôn ngữ GenZ Hà Nội (ông - tôi, nhé, chill, cháy phố...), thân thiện, ngắn gọn.\n" +
                "Bối cảnh người dùng: %s.\n" +
                "Sự kiện đang diễn ra: %s.\n" +
                "Người dùng nói: \"%s\".\n" +
                "Cấu trúc JSON bắt buộc: {introduction, timeline:[{time, activity, placeId, note, estimatedCost}], summary, suggestedPlaceIds:[]}",
                currentTimeStr, placesPrompt, userContext, eventsContext, userMessage
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
            Map<?, ?> response = Mono.defer(() -> {
                String activeKey = getCurrentKey();
                String finalUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + activeKey;
                
                return geminiWebClient.post()
                        .uri(finalUrl)
                        .bodyValue(body)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> 
                            clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error(">>> Gemini API Error Body: {}", errorBody);
                                if (clientResponse.statusCode().value() == 429) {
                                    rotateKey(); // Đổi key khi gặp lỗi 429
                                }
                                return clientResponse.createException();
                            })
                        )
                        .bodyToMono(Map.class);
            })
            // Thử lại tối đa bằng tổng số key * 2 lần, mỗi lần cách nhau 1 giây
            .retryWhen(reactor.util.retry.Retry.backoff(apiKeys.size() * 2L, Duration.ofSeconds(1))
                    .filter(throwable -> 
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests ||
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
                    )
                    .doBeforeRetry(retrySignal -> log.warn(">>> Retrying Gemini API... Attempt: {}", retrySignal.totalRetries() + 1)))
            .block(Duration.ofSeconds(60));

            if (response == null || !response.containsKey("candidates")) {
                throw new RuntimeException("AI response error");
            }

            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            String aiText = (String) firstPart.get("text");

            // Xử lý JSON từ AI
            if (aiText != null) {
                String cleanJson = aiText.replaceAll("```json|```", "").trim();
                int start = cleanJson.indexOf("{");
                int end = cleanJson.lastIndexOf("}");
                if (start != -1 && end != -1) {
                    cleanJson = cleanJson.substring(start, end + 1);
                    return objectMapper.readValue(cleanJson, ChatResponse.class);
                }
            }
            
            return ChatResponse.builder()
                    .introduction(aiText)
                    .timeline(new ArrayList<>())
                    .summary("")
                    .suggestedPlaceIds(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error(">>> AI ERROR: ", e);
            return ChatResponse.builder()
                    .introduction("Xin lỗi, tôi đang xử lý hơi chậm. Bạn thử lại nhé!")
                    .timeline(new ArrayList<>())
                    .build();
        }
    }
}
