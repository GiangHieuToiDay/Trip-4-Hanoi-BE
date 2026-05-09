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
    @PreAuthorize("isAuthenticated()")
    public void handleChatMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        log.info("[WS] Message from {}: {}", authentication.getName(), request.getContent());
        chatService.sendMessageByEmail(authentication.getName(), request);
    }

    /**
     * Staff tiếp nhận phòng chat đang chờ (PENDING -> ACTIVE).
     * Destination: /app/chat.claimRoom.{roomId}
     */
    @MessageMapping("/chat.claimRoom.{roomId}")
    @PreAuthorize("hasAuthority('APPROVE_CHAT')")
    public void handleClaimRoom(@DestinationVariable Long roomId, Authentication authentication) {
        log.info("[WS] Staff {} claiming room {}", authentication.getName(), roomId);
        chatService.claimRoomByEmail(authentication.getName(), roomId);
    }

    /**
     * Nhân viên/Admin gửi ghi chú nội bộ (Khách hàng không thấy).
     * Destination: /app/chat.addNote.{roomId}
     */
    @MessageMapping("/chat.addNote.{roomId}")
    @PreAuthorize("hasAuthority('MANAGE_CHAT')")
    public void handleInternalNote(@DestinationVariable Long roomId,
                                   @Payload InternalNoteRequest request,
                                   Authentication authentication) {
        log.info("[WS] Internal note by {} for room {}: {}", authentication.getName(), roomId, request.getContent());
        chatService.addInternalNoteByEmail(authentication.getName(), roomId, request);
    }
}
