package com.trip4hanoi.app.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationAnalyticsResponse {

    /**
          * Map giữa Khung giờ (0-23) và Số lượng tin nhắn.
          * Ví dụ: {8: 150, 20: 500} -> Khách chat nhiều nhất lúc 8h sáng và 8h tối.
     */
    private Map<Integer, Long> chatVolumeByHour;


    /**
           * Danh sách các từ khóa hoặc chủ đề nổi bật được trích xuất bởi AI.
           * Ví dụ: ["Giá vé", "Thời tiết Hà Nội", "Địa điểm ăn uống"]
     **/
    private List<String> aiTopKeywords;
}
