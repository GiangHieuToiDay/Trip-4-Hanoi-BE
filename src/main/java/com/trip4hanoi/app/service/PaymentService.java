package com.trip4hanoi.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip4hanoi.app.common.PaymentStatus;
import com.trip4hanoi.app.common.PlanType;
import com.trip4hanoi.app.dto.req.CreatePaymentRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PaymentOrderResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PAYMENT-SERVICE")
public class PaymentService {
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.baseurl}")
    private String baseurl;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Transactional
    public PaymentResponse createPaymentLink(CreatePaymentRequest request, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Xác định số tiền dựa trên gói
        int amount = (request.getPackageType() == PlanType.PRO_1_MONTH) ? 150000 : 400000;
        String description = "Thanh toan goi " + request.getPackageType();

        // Tạo mã đơn hàng ngẫu nhiên (10 chữ số)
        long orderCode = Long.parseLong(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        long expiredAt = (System.currentTimeMillis() / 1000) + (15 * 60); // 15 phút từ bây giờ

        // Lưu vào database của mình trước với trạng thái PENDING
        PaymentOrder order = PaymentOrder.builder()
                .orderCode(String.valueOf(orderCode))
                .user(user)
                .amount(amount)
                .packageType(request.getPackageType())
                .status(PaymentStatus.PENDING)
                .build();

        paymentOrderRepository.save(order);

        // --- GỌI API PAYOS TRỰC TIẾP QUA REST TEMPLATE (NÉ LỖI SDK) ---
        String returnUrl = baseurl + "/payment/success";
        String cancelUrl = baseurl + "/payment/cancel";

        try {
            //  Tạo dữ liệu chữ ký theo yêu cầu của PayOS (Alphabetical order)
            String signatureData = "amount=" + amount + 
                                 "&cancelUrl=" + cancelUrl + 
                                 "&description=" + description + 
                                 "&orderCode=" + orderCode + 
                                 "&returnUrl=" + returnUrl;
            
            String signature = calculateHmacSha256(signatureData, checksumKey);

            //  Tạo Body Request
            Map<String, Object> body = new HashMap<>();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);
            body.put("signature", signature);
            body.put("expiredAt", expiredAt);

            //  Cấu hình Headers
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            log.info("[PAYOS-NATIVE] Creating payment link for order: {}", orderCode);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api-merchant.payos.vn/v2/payment-requests", 
                entity, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                String checkoutUrl = root.path("data").path("checkoutUrl").asText();

                return PaymentResponse.builder()
                        .orderCode(String.valueOf(orderCode))
                        .checkoutUrl(checkoutUrl)
                        .amount(amount)
                        .expiredAt(expiredAt)
                        .build();
            } else {
                throw new RuntimeException("PayOS API returned: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("[PAYOS-ERROR] Native API Call failed: {}", e.getMessage());
            throw new AppException(ErrorCode.PAYMENT_LINK_CREATION_FAILED);
        }
    }

    @Transactional
    public void processWebhook(Webhook webhook) throws Exception {
        // Vẫn dùng SDK để verify data (vì phần này thường không bị lỗi expiredAt)
        WebhookData data = payOS.verifyPaymentWebhookData(webhook);

        PaymentOrder order = paymentOrderRepository.findByOrderCode(String.valueOf(data.getOrderCode()))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == PaymentStatus.PENDING) {
            order.setStatus(PaymentStatus.SUCCESS);
            order.setPayosOrderCode(data.getOrderCode());
            paymentOrderRepository.save(order);
            Subscription sub = updateUserSubscription(order.getUser(), order.getPackageType());

            // Xóa cache dashboard để cập nhật doanh thu mới
            clearDashboardCache();

            // Gửi thông báo cho người dùng
            try {
                String packageName = (order.getPackageType() == PlanType.PRO_1_MONTH) ? "PRO 1 Tháng" : "PRO 3 Tháng";
                String message = String.format("Chúc mừng! Bạn đã đăng ký thành công gói %s. Thời hạn sử dụng đến hết ngày %s.",
                        packageName, sub.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                notificationService.createNotification(com.trip4hanoi.app.dto.req.NotificationRequest.builder()
                        .userId(order.getUser().getId())
                        .title("Thanh toán thành công")
                        .message(message)
                        .type("SYSTEM")
                        .status("UNREAD")
                        .build());
            } catch (Exception e) {
                log.error("[PAYMENT-NOTIFICATION] Failed to send notification: {}", e.getMessage());
            }
        }
    }

    // Hàm hỗ trợ tính chữ ký HMAC-SHA256
    private String calculateHmacSha256(String data, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        
        StringBuilder result = new StringBuilder();
        for (byte b : rawHmac) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentOrderResponse> getAllPaymentOrdersAdmin(int page, int size, String keyword, String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
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
                .createdAt(order.getCreationDate())
                .updatedAt(order.getUpdateDate())
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

            if (newStatus == PaymentStatus.SUCCESS) {
                updateUserSubscription(order.getUser(), order.getPackageType());
                clearDashboardCache();
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status");
        }
    }

    private Subscription updateUserSubscription(User user, PlanType packageType) {
        Subscription subscription = subscriptionRepository.findByUser(user)
                .orElse(Subscription.builder()
                        .user(user)
                        .planType(PlanType.FREE)
                        .isActive(false)
                        .build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = (subscription.getEndDate() != null && subscription.getEndDate().isAfter(now)) 
                ? subscription.getEndDate() : now;

        int daysToAdd = (packageType == PlanType.PRO_1_MONTH) ? 30 : 90;
        LocalDateTime endDate = startDate.plusDays(daysToAdd);

        subscription.setPlanType(packageType);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setIsActive(true);

        return subscriptionRepository.save(subscription);
    }

    private void clearDashboardCache() {
        try {
            Set<String> keys = redisTemplate.keys("dashboard_v2::*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[CACHE] Cleared dashboard cache due to successful payment");
            }
        } catch (Exception e) {
            log.error("[CACHE-ERROR] Failed to clear dashboard cache: {}", e.getMessage());
        }
    }
}
