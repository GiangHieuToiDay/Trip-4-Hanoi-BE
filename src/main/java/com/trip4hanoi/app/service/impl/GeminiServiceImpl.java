package com.trip4hanoi.app.service.impl;

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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class  GeminiServiceImpl implements GeminiService {

    private final WebClient geminiWebClient;
    private final PlaceRepository placeRepository;
    
    // Tự khởi tạo thủ công với Jackson 3 (tools.jackson)
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public ChatResponse chatWithAI(String userMessage) {
        List<Place> places = placeRepository.findAll();

        String placesContext = places.stream()
                .map(p -> String.format("- %s [ID: %d]: %s, Địa chỉ: %s, Giá TB: %d",
                        p.getName(), p.getId(), p.getDescription(), p.getAddress(), p.getPriceAvg() != null ? p.getPriceAvg() : 0))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                "Bạn là chuyên gia tư vấn du lịch Hà Nội. Dựa trên danh sách địa điểm này:\n%s\n\n" +
                "Trả lời câu hỏi: \"%s\".\n\n" +
                "QUY TẮC PHẢN HỒI: TRẢ VỀ DUY NHẤT MỘT ĐỐI TƯỢNG JSON (Không thêm văn bản bên ngoài):\n" +
                "{\n" +
                "  \"introduction\": \"...\",\n" +
                "  \"timeline\": [{\"time\": \"...\", \"activity\": \"...\", \"placeId\": 1, \"note\": \"...\", \"estimatedCost\": 0}],\n" +
                "  \"summary\": \"...\",\n" +
                "  \"suggestedPlaceIds\": [1, 2]\n" +
                "}",
                placesContext, userMessage
        );

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
                throw new RuntimeException("Phản hồi không hợp lệ từ Google");
            }

            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            String aiText = (String) firstPart.get("text");

            // Làm sạch JSON thô
            int start = aiText.indexOf("{");
            int end = aiText.lastIndexOf("}");
            if (start != -1 && end != -1) {
                String cleanJson = aiText.substring(start, end + 1);
                return mapper.readValue(cleanJson, ChatResponse.class);
            }
            
            return ChatResponse.builder().introduction(aiText).timeline(new ArrayList<>()).summary("").suggestedPlaceIds(new ArrayList<>()).build();

        } catch (Exception e) {
            log.error(">>> LỖI GEMINI: ", e);
            return ChatResponse.builder().introduction("Lỗi xử lý AI: " + e.getMessage()).timeline(new ArrayList<>()).build();
        }
    }
}
