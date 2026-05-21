package com.trip4hanoi.app.service;


import com.trip4hanoi.app.common.PaymentStatus;
import com.trip4hanoi.app.common.PlanType;
import com.trip4hanoi.app.dto.req.CreatePaymentRequest;
import com.trip4hanoi.app.dto.res.PaymentResponse;
import com.trip4hanoi.app.entity.PaymentOrder;
import com.trip4hanoi.app.entity.Subscription;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.PaymentOrderRepository;
import com.trip4hanoi.app.repository.SubscriptionRepository;
import com.trip4hanoi.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PAYMENT-SERVICE")
public class PaymentService {
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${app.baseurl}")
    private String baseurl;


    @Transactional
    public PaymentResponse createPaymentLink(CreatePaymentRequest request, Long userId) throws Exception{
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Xác định số tiền dựa trên gói
        int amount = (request.getPackageType() == PlanType.PRO_1_MONTH) ? 150000 : 400000;
        String description = "Thanh toan goi " + request.getPackageType();

        // Tao  ma don hang ngay nhien (So nguyen cho PayOS)
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(1,11));

        // Luu vao database cua minh truoc voi trang thai PENDING;
        PaymentOrder order = PaymentOrder.builder()
                .orderCode(String.valueOf(orderCode))
                .user(user)
                .amount(amount)
                .packageType(request.getPackageType())
                .status(PaymentStatus.PENDING)
                .build();

        paymentOrderRepository.save(order);

        // Goi PayOS tao link
        String returnUrl = baseurl +"/payment/success";
        String cancelUrl = baseurl +"/payment/cancel";

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);

        return PaymentResponse.builder()
                .orderCode(String.valueOf(orderCode))
                .checkoutUrl(data.getCheckoutUrl())
                .amount(amount)
                .build();
    }




    public void processWebhook(Webhook webhook) throws Exception{
        //xac thuc chu ky tu PAYos (DUNG SDK de verify )
        WebhookData data = payOS.verifyPaymentWebhookData(webhook);

        //tim don hang trong db cua minh
        PaymentOrder order = paymentOrderRepository.findByOrderCode(String.valueOf(data.getOrderCode()))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // neu don hang chua thanh cong thi moi xu ly
        if(order.getStatus() == PaymentStatus.PENDING){
            order.setStatus(PaymentStatus.SUCCESS);
            order.setPayosOrderCode(data.getOrderCode());
            paymentOrderRepository.save(order);

            // Cap nhat hoa tao moi subscription cho User
            updateUserSubscription(order.getUser(),order.getPackageType());
        }

    }

import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PaymentOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// ... inside PaymentService class ...

    @Transactional(readOnly = true)
    public PageResponse<PaymentOrderResponse> getAllPaymentOrdersAdmin(int page, int size, String keyword, String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        PaymentStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Page<PaymentOrder> orderPage = paymentOrderRepository.searchOrdersAdmin(keyword, statusEnum, pageable);

        List<PaymentOrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PaymentOrderResponse>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .data(content)
                .build();
    }

    private PaymentOrderResponse mapToResponse(PaymentOrder order) {
        return PaymentOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .payosOrderCode(order.getPayosOrderCode())
                .username(order.getUser().getUsername())
                .email(order.getUser().getEmail())
                .packageType(order.getPackageType())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Transactional
    public void updateOrderStatus(Long id, String status) {
        PaymentOrder order = paymentOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            paymentOrderRepository.save(order);
            
            // Nếu Admin chuyển sang SUCCESS thủ công, hãy nâng cấp cho User
            if (newStatus == PaymentStatus.SUCCESS) {
                updateUserSubscription(order.getUser(), order.getPackageType());
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status");
        }
    }
































}
