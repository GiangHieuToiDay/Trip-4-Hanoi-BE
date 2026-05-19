package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.dto.res.ChatRoomResponse;
import com.trip4hanoi.app.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j(topic = "CHAT-REST-CONTROLLER")
@Tag(name = "Chat Management", description = "APIs for managing chat rooms and retrieving message history")
public class ChatRestController {

    private final ChatService chatService;

    @Operation(summary = "Get chat history", description = "Retrieve a list of messages for a specific chat room")
    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<List<ChatMessageResponse>>> getChatHistory(@PathVariable Long roomId) {
        log.info("Fetching chat history for room ID: {}", roomId);
        
        List<ChatMessageResponse> messages = chatService.getChatHistory(roomId);
        
        return ResponseEntity.ok(APIResponse.<List<ChatMessageResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get chat history successfully")
                .data(messages)
                .build());
    }

    @Operation(summary = "Get rooms by status", description = "Retrieve a list of chat rooms filtered by status (PENDING, ACTIVE, CLOSED)")
    @GetMapping("/rooms")
    @PreAuthorize("hasAnyAuthority('APPROVE_CHAT', 'MANAGE_CHAT')")
    public ResponseEntity<APIResponse<List<ChatRoomResponse>>> getRooms(@RequestParam String status) {
        log.info("Fetching rooms with status: {}", status);
        
        List<ChatRoomResponse> rooms = chatService.getRoomByStatus(status);
        
        return ResponseEntity.ok(APIResponse.<List<ChatRoomResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get rooms list successfully")
                .data(rooms)
                .build());
    }

    @Operation(summary = "Get current user's active room", description = "Retrieve the active chat room for the authenticated user")
    @GetMapping("/my-room")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ChatRoomResponse>> getMyActiveRoom(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ChatRoomResponse room = chatService.getActiveRoomForUserByEmail(authentication.getName());
        return ResponseEntity.ok(APIResponse.<ChatRoomResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Get active room successfully")
                .data(room)
                .build());
    }
}
