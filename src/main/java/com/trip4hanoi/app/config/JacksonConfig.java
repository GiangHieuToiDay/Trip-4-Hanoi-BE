package com.trip4hanoi.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary
       public ObjectMapper objectMapper() {
                 ObjectMapper mapper = new ObjectMapper();
               // Ép Java bỏ qua mọi trường lạ (như expiredAt) trên toàn hệ thống
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return mapper;
           }
}
