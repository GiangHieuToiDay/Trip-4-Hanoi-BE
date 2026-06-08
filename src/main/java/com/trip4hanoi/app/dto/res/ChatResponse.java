package com.trip4hanoi.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String intent; // CHAT or PLAN
    private String introduction; // Lời chào
    private String estimatedBudget; // Ngân sách dự kiến
    private List<ScheduleItem> timeline; // Timeline các bước
    private String summary; // Tổng kết chi phí và lời chúc
    private List<Long> suggestedPlaceIds; // List ID để Frontend fetch thêm ảnh/đánh giá
}
