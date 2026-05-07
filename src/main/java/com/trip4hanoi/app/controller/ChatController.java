package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ChatRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

//    @PostMapping
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
//        ChatResponse response = geminiService.chatWithAI(request.getMessage());
//        return ResponseEntity.ok(response);
//    }

    //@Operation(summary = "Chat with AI", description = "API send message to AI and receive response")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {

        ChatResponse chatResponse = geminiService.chatWithAI(request.getMessage());

        APIResponse<ChatResponse> response = APIResponse.<ChatResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Chat successfully")
                .data(chatResponse)
                .build();

        return ResponseEntity.ok(response);
    }

}
