package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
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
        boolean needsRomantic = lowerMsg.contains("người yêu") || lowerMsg.contains("hẹn hò") || lowerMsg.contains("lãng mạn") || lowerMsg.contains("vợ");
        boolean needsFood = lowerMsg.contains("ăn") || lowerMsg.contains("đói") || lowerMsg.contains("nhà hàng") || lowerMsg.contains("quán");

        // Chạy song song các tác vụ lấy dữ liệu (Parallel Fetching)
        CompletableFuture<List<PlaceResponse>> recommendedPlacesFuture = CompletableFuture.supplyAsync(() -> {
            List<PlaceResponse> places = new ArrayList<>();
            
            // 1. Lấy gợi ý cá nhân hóa diện rộng (30 địa điểm)
            List<?> rawList = recommendationService.getPersonalizedRecommendations(30);
            places.addAll(rawList.stream()
                    .map(item -> objectMapper.convertValue(item, PlaceResponse.class))
                    .collect(Collectors.toList()));

            // 2. Chủ động bổ sung địa điểm theo mục tiêu (Top Districts: Hoàn Kiếm, Tây Hồ, Cầu Giấy)
            if (places.size() < 40) {
                List<Place> allPlaces = placeRepository.findAllByDeletedFalse();
                
                // Lấy thêm Homestay nếu cần
                if (needsHomestay) {
                    allPlaces.stream()
                        .filter(p -> p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase("Homestay"))
                        .limit(5)
                        .forEach(p -> addIfAbsent(places, p));
                }

                // Lấy thêm địa điểm "Sang chảnh/Chill" ở trung tâm nếu ngân sách lớn hoặc đi với người yêu
                allPlaces.stream()
                    .filter(p -> p.getDistrict() != null && (p.getDistrict().contains("Hoàn Kiếm") || p.getDistrict().contains("Tây Hồ")))
                    .filter(p -> p.getRatingAvg() != null && p.getRatingAvg() >= 4.0)
                    .limit(10)
                    .forEach(p -> addIfAbsent(places, p));
                    
                // Lấy thêm địa điểm ăn uống nếu đang đói
                if (needsFood) {
                    allPlaces.stream()
                        .filter(p -> p.getCategory() != null && (p.getCategory().getName().contains("Restaurant") || p.getCategory().getName().contains("Food")))
                        .limit(5)
                        .forEach(p -> addIfAbsent(places, p));
                }
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
            
            return String.format("User: %s, Gu: %s, Khu vực quen thuộc: %s. Thời gian hiện tại: %s, %s.",
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

        // Prompt Compression & Enrichment
        String placesPrompt = recommendedPlaces.stream()
                .map(p -> String.format("[%d]%s(%s)-%s:%s.Giá:%d", 
                        p.getId(), p.getName(), p.getDistrict(), p.getCategoryName() != null ? p.getCategoryName() : "Khác",
                        p.getDescription(), p.getPriceAvg()))
                .collect(Collectors.joining("|"));

        String prompt = String.format(
                "Bạn là 'Local Buddy' - Chuyên gia bản địa Hà Nội.\n" +
                "QUY TẮC TỐI THƯỢNG:\n" +
                "- Chỉ trả về JSON hợp lệ.\n" +
                "- Không markdown.\n" +
                "- Không giải thích ngoài JSON.\n" +
                "- Không hiển thị ID địa điểm trong bất kỳ trường text nào.\n" +
                "- Mọi địa điểm chỉ hiển thị tên thân thiện.\n" +
                "\n" +
                "THỨ TỰ ƯU TIÊN: 1. Format JSON, 2. Phân loại ý định, 3. Logic thời gian thực, 4. Chất lượng trải nghiệm, 5. Persona.\n" +
                "\n" +
                "THỜI GIAN HIỆN TẠI: %s, %s.\n" +
                "BỐI CẢNH NGƯỜI DÙNG: %s.\n" +
                "DANH SÁCH ĐỊA ĐIỂM: %s.\n" +
                "SỰ KIỆN: %s.\n" +
                "\n" +
                "PHÂN LOẠI Ý ĐỊNH:\n" +
                "- intent = CHAT: Chào hỏi, tán gẫu, hỏi năng lực... \n" +
                "  Kết quả: { \"intent\":\"CHAT\", \"introduction\":\"...\", \"timeline\":[] }\n" +
                "- intent = PLAN: Muốn đi chơi, hẹn hò, cần lịch trình, tìm quán...\n" +
                "  Kết quả: { \"intent\":\"PLAN\", \"introduction\":\"...\", \"timeline\":[...] }\n" +
                "\n" +
                "NGUYÊN TẮC LẬP KẾ HOẠCH:\n" +
                "1. Ưu tiên trải nghiệm thực tế theo thời gian hiện tại:\n" +
                "   - 06h-10h: ăn sáng, cafe sáng.\n" +
                "   - 11h-13h: ăn trưa.\n" +
                "   - 14h-17h: tham quan, workshop, cafe.\n" +
                "   - 18h-20h: ăn tối.\n" +
                "   - 20h-23h: rooftop, bar, phố đi bộ, chill.\n" +
                "2. Không đề xuất: Đền/chùa sau 18h, Bảo tàng sau giờ đóng cửa, Cinema trước 10h sáng, Bar trước 17h.\n" +
                "3. Ngân sách:\n" +
                "   - <500k: food tour, trà chanh, công viên.\n" +
                "   - 500k-1tr: cafe đẹp, ăn nhà hàng tầm trung.\n" +
                "   - >1tr: rooftop, fine dining, workshop, cinema VIP, trải nghiệm đặc biệt.\n" +
                "4. Có thể di chuyển giữa nhiều khu vực nếu trải nghiệm đáng giá và ngân sách cho phép.\n" +
                "5. Mỗi hoạt động phải có: thời gian, địa điểm, mô tả ngắn, chi phí ước tính.\n" +
                "\n" +
                "PERSONA:\n" +
                "- Giọng điệu Gen Z Hà Nội.\n" +
                "- Xưng hô: ông - tôi.\n" +
                "- Thân thiện, lịch sự, không lạm dụng slang.\n" +
                "\n" +
                "SCHEMA JSON:\n" +
                "{\n" +
                "  \"intent\": \"CHAT|PLAN\",\n" +
                "  \"introduction\": \"string\",\n" +
                "  \"estimatedBudget\": \"string\",\n" +
                "  \"summary\": \"string\",\n" +
                "  \"suggestedPlaceIds\": [number],\n" +
                "  \"timeline\": [\n" +
                "    {\n" +
                "      \"time\": \"HH:mm\",\n" +
                "      \"activity\": \"string\",\n" +
                "      \"placeId\": number,\n" +
                "      \"note\": \"string\",\n" +
                "      \"estimatedCost\": \"string\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Người dùng: \"%s\"",
                currentDayOfWeek, currentTimeStr, userContext, placesPrompt, eventsContext, userMessage
        );

        log.info("Data fetching & Master Planner preparation took: {} ms", System.currentTimeMillis() - startTime);

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

    private void addIfAbsent(List<PlaceResponse> list, Place p) {
        if (list.stream().noneMatch(item -> item.getId().equals(p.getId()))) {
            list.add(PlaceResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .district(p.getDistrict())
                    .description(p.getDescription())
                    .priceAvg(p.getPriceAvg())
                    .categoryName(p.getCategory() != null ? p.getCategory().getName() : "Khác")
                    .isRecommended(true)
                    .build());
        }
    }

    @Override
    public String simpleChat(String prompt) {
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
                                    rotateKey();
                                }
                                return clientResponse.createException();
                            })
                        )
                        .bodyToMono(Map.class);
            })
            .retryWhen(reactor.util.retry.Retry.backoff(apiKeys.size() * 2L, Duration.ofSeconds(1))
                    .filter(throwable -> 
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests ||
                        throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.ServiceUnavailable
                    )
            )
            .block(Duration.ofSeconds(10));

            if (response == null || !response.containsKey("candidates")) {
                return "";
            }

            List<?> candidates = (List<?>) response.get("candidates");
            if (candidates.isEmpty()) return "";
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");

        } catch (Exception e) {
            log.error(">>> simpleChat ERROR: ", e);
            return "";
        }
    }
}
