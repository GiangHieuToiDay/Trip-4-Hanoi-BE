package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.WeatherResponse;
import com.trip4hanoi.app.entity.Event;
import com.trip4hanoi.app.entity.Notification;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.repository.EventRepository;
import com.trip4hanoi.app.repository.NotificationRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.FcmService;
import com.trip4hanoi.app.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DAILY-NOTIFICATION-JOB")
public class DailyNotificationJob {

    private final UserRepository userRepository;
    private final WeatherService weatherService;
    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    /**
     * Chạy lúc 7:00 AM hàng ngày (Asia/Ho_Chi_Minh)
     * Cron: "0 0 7 * * *"
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendMorningNewsletter() {
        log.info("Bắt đầu thực thi Job thông báo buổi sáng lúc: {}", LocalDateTime.now());

        // 1. Lấy thông tin thời tiết Hà Nội
        WeatherResponse weather = weatherService.getWeatherByCity("Hanoi");
        String weatherMsg = String.format("Chào buổi sáng! Hà Nội hôm nay %d°C, %s.", 
                (int)weather.getTemp(), weather.getDescription());

        // 2. Lấy danh sách sự kiện nổi bật hôm nay (đang diễn ra)
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);
        
        // Giả sử lấy 1-2 sự kiện tiêu biểu đang diễn ra
        List<Event> todayEvents = eventRepository.findAll().stream()
                .filter(e -> e.getStartTime().isBefore(todayEnd) && e.getEndTime().isAfter(todayStart))
                .limit(2)
                .toList();

        String eventMsg = "";
        if (!todayEvents.isEmpty()) {
            eventMsg = " Đừng bỏ lỡ sự kiện: " + todayEvents.get(0).getName();
            if (todayEvents.size() > 1) eventMsg += " và các lễ hội khác.";
        } else {
            eventMsg = " Hãy cùng khám phá những địa điểm thú vị hôm nay nhé!";
        }

        String fullMessage = weatherMsg + eventMsg;

        // 3. Gửi cho tất cả User đang hoạt động
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equals(u.getStatus().name()))
                .toList();

        log.info("Đang gửi thông báo cho {} người dùng.", activeUsers.size());

        for (User user : activeUsers) {
            try {
                String title = "Trip4Hanoi: Bản tin sáng sớm \u2615";
                // Lưu vào DB Notification
                Notification notification = Notification.builder()
                        .user(user)
                        .title(title)
                        .message(fullMessage)
                        .type("NEWSLETTER")
                        .status("UNREAD")
                        .build();
                notificationRepository.save(notification);

                // Gửi qua FCM
                fcmService.sendToUser(user, title, fullMessage);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo cho user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("Hoàn thành Job thông báo buổi sáng.");
    }
}
