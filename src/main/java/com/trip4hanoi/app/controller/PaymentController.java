package com.trip4hanoi.app.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trip4hanoi.app.dto.req.CreatePaymentRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PaymentResponse;
import com.trip4hanoi.app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PaymentOrderResponse;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.Webhook;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;


    //API danh cho FE : Goi de lay link thanh toan
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<PaymentResponse>> createPayment (@RequestBody CreatePaymentRequest request) throws Exception{

        Long userId = getCurrentUserId();
        PaymentResponse paymentResponse = paymentService.createPaymentLink(request,userId);

        return ResponseEntity.ok(APIResponse.<PaymentResponse>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Payment Link Created")
                .data(paymentResponse)
                .build());

    }

    // API dành cho PayOS: PayOS sẽ gọi vào đây khi thanh toán thành công
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Object body) {

        // Nếu body là null hoặc trống (PayOS đang test kết nối)
        if (body == null) {
            log.info("[PAYOS] Received empty webhook test");
            return ResponseEntity.ok().build();
        }

        try {
            log.info("[PAYOS] Webhook received: {}", body);

            // Ép kiểu về Map để xử lý linh hoạt
            if (body instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) body;

                // Nếu PayOS chỉ gửi request test
                if (!data.containsKey("code") && !data.containsKey("data")) {
                    log.info("[PAYOS] Webhook Test Connection - Success");
                    return ResponseEntity.ok().build();
                }

                // Chuyển Map thành đối tượng Webhook
                ObjectMapper mapper = new ObjectMapper();
                Webhook webhook = mapper.convertValue(data, Webhook.class);

                paymentService.processWebhook(webhook);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[PAYOS] Error processing webhook: {}", e.getMessage());

            // Luôn trả về 200 để PayOS không báo lỗi
            return ResponseEntity.ok().build();
        }
    }

     // --- ADMIN ENDPOINTS ---

     @GetMapping("/admin/orders")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<APIResponse<PageResponse<PaymentOrderResponse>>> getAllOrdersAdmin(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(required = false) String keyword,
             @RequestParam(required = false) String status) {

         var data = paymentService.getAllPaymentOrdersAdmin(page, size, keyword, status);

         return ResponseEntity.ok(APIResponse.<PageResponse<PaymentOrderResponse>>builder()
                 .status(HttpStatus.OK.value())
                 .code(1000)
                 .message("Get all orders successfully")
                 .data(data)
                 .build());
     }

     @PutMapping("/admin/orders/{id}/status")
     @PreAuthorize("hasRole('ADMIN')")
     public ResponseEntity<APIResponse<Void>> updateOrderStatus(
             @PathVariable Long id,
             @RequestParam String status) {

         paymentService.updateOrderStatus(id, status);

         return ResponseEntity.ok(APIResponse.<Void>builder()
                 .status(HttpStatus.OK.value())
                 .code(1000)
                 .message("Update order status successfully")
                 .build());
     }

     private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var  authentication = context.getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof  Jwt jwt){
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }

}
