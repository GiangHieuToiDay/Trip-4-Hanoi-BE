package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ChatMessageRequest;
import com.trip4hanoi.app.dto.req.InternalNoteRequest;
import com.trip4hanoi.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "CHAT-SOCKET-CONTROLLER")
public class ChatSocketController {

    private final ChatService chatService;

    /**
     * User hoặc Staff gửi tin nhắn vào phòng.
     * Destination: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        if (authentication == null) {
            log.error("[WS-ERROR] Unauthorized attempt to send message");
            return;
        }
        log.info("[WS] Message from {}: {}", authentication.getName(), request.getContent());
        chatService.sendMessageByEmail(authentication.getName(), request);
    }

    /**
     * Staff tiếp nhận phòng chat đang chờ (PENDING -> ACTIVE).
     * Destination: /app/chat.claimRoom.{roomId}
     */
    @MessageMapping("/chat.claimRoom.{roomId}")
    public void handleClaimRoom(@DestinationVariable Long roomId, Authentication authentication) {
        if (authentication == null || !hasAuthority(authentication, "APPROVE_CHAT")) {
            log.error("[WS-ERROR] User {} has no authority to claim room", 
                authentication != null ? authentication.getName() : "Anonymous");
            return;
        }
        log.info("[WS] Staff {} claiming room {}", authentication.getName(), roomId);
        chatService.claimRoomByEmail(authentication.getName(), roomId);
    }

    /**
     * Nhân viên/Admin gửi ghi chú nội bộ (Khách hàng không thấy).
     * Destination: /app/chat.addNote.{roomId}
     */
    @MessageMapping("/chat.addNote.{roomId}")
    public void handleInternalNote(@DestinationVariable Long roomId,
                                   @Payload InternalNoteRequest request,
                                   Authentication authentication) {
        if (authentication == null || !hasAuthority(authentication, "MANAGE_CHAT")) {
            log.error("[WS-ERROR] User {} has no authority to add internal notes", 
                authentication != null ? authentication.getName() : "Anonymous");
            return;
        }
        log.info("[WS] Internal note by {} for room {}: {}", authentication.getName(), roomId, request.getContent());
        chatService.addInternalNoteByEmail(authentication.getName(), roomId, request);
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
