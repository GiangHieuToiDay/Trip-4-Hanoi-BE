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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "GEMINI SERVICE")
public class  GeminiServiceImpl implements GeminiService {

    private final WebClient geminiWebClient;
    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    
    // Tự khởi tạo thủ công với Jackson 3 (tools.jackson)
    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Override
    public ChatResponse chatWithAI(String userMessage) {
        List<Place> places = placeRepository.findAllByDeletedFalse();
        List<Event> events = eventRepository.findAll();

        String placesContext = places.stream()
                .map(p -> String.format("- %s [ID: %d]: %s, Địa chỉ: %s, Giá TB: %d",
                        p.getName(), p.getId(), p.getDescription(), p.getAddress(), p.getPriceAvg() != null ? p.getPriceAvg() : 0))
                .collect(Collectors.joining("\n"));

        String eventsContext = events.stream()
                .map(e -> String.format("- Sự kiện: %s diễn ra tại %s [ID địa điểm: %d]. Thời gian: từ %s đến %s. Mô tả: %s",
                        e.getName(), e.getPlace().getName(), e.getPlace().getId(), e.getStartTime(), e.getEndTime(), e.getDescription()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                "Bạn là 'Người bạn bản địa số' tư vấn du lịch Hà Nội. Bạn có thông tin về các địa điểm và các sự kiện đang diễn ra.\n\n" +
                        "DANH SÁCH ĐỊA ĐIỂM:\n%s\n\n" +
                        "DANH SÁCH SỰ KIỆN ĐANG DIỄN RA:\n%s\n\n" +
                        "NHIỆM VỤ: Trả lời câu hỏi của người dùng: \"%s\".\n" +
                        "YÊU CẦU ĐẶC BIỆT: Nếu có sự kiện diễn ra tại địa điểm bạn gợi ý, hãy ưu tiên nhắc đến sự kiện đó trong phần 'introduction' và 'note' của timeline để khuyến khích người dùng tham gia.\n\n" +
                        "QUY TẮC PHẢN HỒI: TRẢ VỀ DUY NHẤT MỘT ĐỐI TƯỢNG JSON:\n" +
                        "{\n" +
                        "  \"introduction\": \"Chào mừng bạn đến với Hà Nội! Hôm nay ở Văn Miếu có...\",\n" +
                        "  \"timeline\": [{\"time\": \"...\", \"activity\": \"...\", \"placeId\": 1, \"note\": \"Lưu ý: Có lễ hội Chữ Xuân đang diễn ra tại đây!\", \"estimatedCost\": 0}],\n" +
                        "  \"summary\": \"Hy vọng bạn có chuyến đi vui vẻ...\",\n" +
                        "  \"suggestedPlaceIds\": [1, 2]\n" +
                        "}",
                placesContext,eventsContext, userMessage
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
