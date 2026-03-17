package com.trip4hanoi.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {
    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder().build(); // Không để baseUrl ở đây nữa để tránh nhầm lẫn
    }
}
