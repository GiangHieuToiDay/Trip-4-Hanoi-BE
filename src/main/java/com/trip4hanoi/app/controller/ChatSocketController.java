package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.req.ChatMessageRequest;
import com.trip4hanoi.app.dto.req.InternalNoteRequest;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {

    private final ChatService chatService;

    //  User hoặc Staff gửi tin nhắn vào phòng
    // Client gửi đến: /app/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(@Payload ChatMessageRequest request, Authentication authentication) {
        log.info("Received chat message from user: {}, content: {}", authentication.getName(), request.getContent());
        // Lấy User entity từ Service thông qua email (getName() thường trả về email/username trong JWT)
        chatService.sendMessageByEmail(authentication.getName(), request);
    }

    //  Staff nhấn tiếp nhận phòng chat
    // Client gửi đến: /app/chat.claimRoom.{roomId}
    @MessageMapping("/chat.claimRoom.{roomId}")
    public void handleClaimRoom(@DestinationVariable Long roomId, Authentication authentication) {
        log.info("User {} claiming room {}", authentication.getName(), roomId);
        chatService.claimRoomByEmail(authentication.getName(), roomId);
    }

    //  Admin/Staff gửi ghi chú nội bộ
    // Client gửi đến: /app/chat.addNote.{roomId}
    @MessageMapping("/chat.addNote.{roomId}")
    public void handleInternalNote(@DestinationVariable Long roomId,
                                   @Payload InternalNoteRequest request,
                                   Authentication authentication) {
        log.info("User {} adding internal note to room {}", authentication.getName(), roomId);
        chatService.addInternalNoteByEmail(authentication.getName(), roomId, request);
    }
}
