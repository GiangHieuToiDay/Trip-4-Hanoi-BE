package com.trip4hanoi.app.service.impl;


import com.trip4hanoi.app.entity.Event;
import com.trip4hanoi.app.entity.EventSubscription;
import com.trip4hanoi.app.entity.Notification;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.repository.EventRepository;
import com.trip4hanoi.app.repository.EventSubscriptionRepository;
import com.trip4hanoi.app.repository.NotificationRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.EventReminderService;
import com.trip4hanoi.app.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventReminderServiceImpl implements EventReminderService {

    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendEventReminder() {
        log.info("Checking for due event reminders...");
        LocalDateTime now = LocalDateTime.now();
        List<EventSubscription> list = eventSubscriptionRepository.findDueNotifications(now);

        for (var sub : list) {
            try {
                Event event = eventRepository.findById(sub.getEventId()).orElseThrow();
                User user = userRepository.findById(sub.getUserId()).orElseThrow();

                String message = "Sự kiện sắp diễn ra: " + event.getName();

                // 1. lưu DB
                Notification noti = Notification.builder()
                        .user(user)
                        .event(event)
                        .message(message)
                        .status("UNREAD")
                        .createdAt(LocalDateTime.now())
                        .build();

                notificationRepository.save(noti);

                // 2. gửi FCM
                fcmService.sendToUser(user, "Nhắc lịch sự kiện", message);

                // 3. mark success
                sub.setNotified(true);
                log.info("Successfully sent reminder for event {} to user {}", event.getId(), user.getId());

            } catch (Exception e) {
                log.error("Error sending reminder for subscription {}: {}", sub.getId(), e.getMessage());
                // retry logic
                sub.setRetryCount(sub.getRetryCount() + 1);
                sub.setLastAttemptAt(LocalDateTime.now());

                if (sub.getRetryCount() >= 3) {
                    sub.setNotified(true); // bỏ luôn tránh loop vô hạn
                    log.warn("Subscription {} reached max retries, marked as notified", sub.getId());
                }
            }

            eventSubscriptionRepository.save(sub);
        }
    }

    @Override
    @Transactional
    public void updateEvent(Event event) {
        eventRepository.save(event);

        // reset lại notify
        List<EventSubscription> subs = eventSubscriptionRepository.findByEventId(event.getId());

        for (var sub : subs) {
            sub.setNotified(false);
            sub.setRetryCount(0);
        }

        eventSubscriptionRepository.saveAll(subs);
    }
}
