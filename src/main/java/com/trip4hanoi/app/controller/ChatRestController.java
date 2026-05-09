package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.dto.res.ChatRoomResponse;
import com.trip4hanoi.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    // Lấy lịch sử tin nhắn của 1 phòng
    @GetMapping("/rooms/{roomId}/messages")
    public APIResponse<List<ChatMessageResponse>> getChatHistory(@PathVariable Long roomId) {
        return APIResponse.<List<ChatMessageResponse>>builder()
                .data(chatService.getChatHistory(roomId))
                .build();
    }

    // Lấy danh sách phòng chat theo trạng thái (PENDING, ACTIVE, CLOSED)
    @GetMapping("/rooms")
    public APIResponse<List<ChatRoomResponse>> getRooms(@RequestParam String status) {
        return APIResponse.<List<ChatRoomResponse>>builder()
                .data(chatService.getRoomByStatus(status))
                .build();
    }
}
