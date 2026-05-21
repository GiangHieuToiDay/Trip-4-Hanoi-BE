package com.trip4hanoi.app.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String orderCode;// Mã đơn hàng của mình
    private String checkoutUrl;// Link thanh toán PayOS sinh ra
    private String qrCode;// (Tùy chọn) Một số SDK trả về text QR
    private Integer amount;
}
