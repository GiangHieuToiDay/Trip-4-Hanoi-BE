package com.trip4hanoi.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //endpoint de client ket noi websocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Chấp nhận mọi nguồn truy cập
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // các dường dẫn bắt đầu bằng /topic hoặc /queue là nơi server gửi tin nhắn về Client
        config.enableSimpleBroker("/topic","/queue");

        // các đường dẫn bắt đầu bằng /app là nơi client gửi tin nhắn đến server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix dành cho tin nhắn cá nhân (user cụ the)
        config.setUserDestinationPrefix("/user");

    }


}
