package com.trip4hanoi.app.dto.res;

import com.trip4hanoi.app.common.PaymentStatus;
import com.trip4hanoi.app.common.PlanType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentOrderResponse {
    private Long id;
    private String orderCode;
    private Long payosOrderCode;
    private String username;
    private String email;
    private PlanType packageType;
    private Integer amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
