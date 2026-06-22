package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.ChatResponse;

public interface GeminiService {
    ChatResponse chatWithAI(String userMessage, Long userId);
    String simpleChat(String prompt);
}
