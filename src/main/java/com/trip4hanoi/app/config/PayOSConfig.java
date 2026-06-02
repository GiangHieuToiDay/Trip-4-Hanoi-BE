package com.trip4hanoi.app.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

import java.lang.reflect.Field;

@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;


    @Bean
    public PayOS payOS(){
        PayOS payOS = new PayOS(clientId, apiKey, checksumKey);

                // --- ĐOẠN CODE "MA THUẬT" ĐỂ SỬA LỖI EXPIRED_AT ---
               try {
                      Field field = PayOS.class.getDeclaredField("objectMapper");
                       field.setAccessible(true);
                      ObjectMapper om = (ObjectMapper) field.get(payOS);
                       om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                     System.out.println("[PAYOS-FIX] Successfully forced PayOS to ignore unknown properties!");
                     } catch (Exception e) {
                       System.err.println("[PAYOS-FIX] Failed to fix PayOS ObjectMapper: " + e.getMessage());
                     }
               // ------------------------------------------------

                return payOS;
    }


}
