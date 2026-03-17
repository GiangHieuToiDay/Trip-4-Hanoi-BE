package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.ChatRequest;
import com.trip4hanoi.app.dto.res.ChatResponse;
import com.trip4hanoi.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = geminiService.chatWithAI(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
