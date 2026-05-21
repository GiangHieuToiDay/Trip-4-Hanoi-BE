package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ChatRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.service.GeminiService;
import com.trip4hanoi.app.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatAIController {

    private final GeminiService geminiService;
    private final SubscriptionService subscriptionService;

    //@Operation(summary = "Chat with AI", description = "API send message to AI and receive response")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {

        Long userId = getCurrentUserId();

        // --- BẮT ĐẦU PAYWALL ---
        if (!subscriptionService.canUserChat(userId)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(APIResponse.<ChatResponse>builder()
                            .status(HttpStatus.PAYMENT_REQUIRED.value())
                            .code(4002)
                            .message("Bạn đã hết lượt chat miễn phí hôm nay. Vui lòng nâng cấp gói PRO!")
                    .build());
        }
        //========


        ChatResponse chatResponse = geminiService.chatWithAI(request.getMessage(), userId);

        // Chat thành công thì tăng số lượt đã dùng
        subscriptionService.incrementChatCount(userId);

        APIResponse<ChatResponse> response = APIResponse.<ChatResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Chat successfully")
                .data(chatResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }
}
