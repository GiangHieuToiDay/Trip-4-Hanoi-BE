package com.trip4hanoi.app.service;


import com.trip4hanoi.app.entity.Subscription;
import com.trip4hanoi.app.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SUBSCRIPTION-SERVICE")
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StringRedisTemplate redisTemplate;


    private static final int FREE_CHAT_LIMIT = 4;

    public  boolean canUserChat(Long userId){
        //kiem tra subscription tron db
        Subscription sub =  subscriptionRepository.findByUserId(userId).orElse(null);


        // neu la pro va con han
        if(sub != null && sub.getIsActive() && sub.getEndDate().isAfter(LocalDateTime.now())){
            return true;
        }

        // neu khong phai pro , kiem tra gioi han trong redis
        String key = "chat_limit: "+ userId + ":" + LocalDate.now();
        String countStr =  redisTemplate.opsForValue().get(key);
        int count = (countStr != null) ? Integer.parseInt(countStr) : 0;

        return  count <= FREE_CHAT_LIMIT;

    }

    public  void incrementChatCount(Long userId){
        //Chi tang dem neu user khog phai la PRO
        Subscription sub =  subscriptionRepository.findByUserId(userId).orElse(null);
        if(sub == null || !sub.getIsActive() || sub.getEndDate().isBefore(LocalDateTime.now())){
            String key = "chat_limit: "+ userId + ":" + LocalDate.now();
            redisTemplate.opsForValue().increment(key);
            // Set thời gian sống cho Key là 24h để tự động reset vào ngày hôm sau
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }


}
