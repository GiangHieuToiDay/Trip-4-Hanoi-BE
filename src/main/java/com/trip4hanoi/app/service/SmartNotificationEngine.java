package com.trip4hanoi.app.service;

import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.repository.NotificationRepository;
import com.trip4hanoi.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.trip4hanoi.app.repository.PlaceRepository;
import java.util.List;

import com.trip4hanoi.app.common.PostStatus;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SMART-NOTIFICATION-ENGINE")
public class SmartNotificationEngine {

    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    // Khoảng cách tối đa để gợi ý (mét)
    private static final double MAX_RADIUS_METERS = 500.0;
    // Ngưỡng rating để coi là địa điểm nổi bật
    private static final double MIN_RATING_FOR_SUGGESTION = 4.5;

    public void notifyAdminNewPost(Post post) {
        log.info("Triggering Notification for Admins: New Post Pending Review");
        
        // Lấy danh sách Admin chuẩn hơn từ Query
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")))
                .toList();

        String title = "Có bài viết mới cần duyệt! \uD83D\uDCE8";
        String message = String.format("Người dùng %s vừa tạo một bài viết mới: '%s'. Vui lòng kiểm tra và duyệt.", 
                post.getUser().getActualUsername(), post.getTitle());
        String targetUrl = "/admin/posts"; // Đường dẫn đến trang duyệt bài của Admin

        for (User admin : admins) {
            saveAndPush(admin, title, message, targetUrl);
        }
    }

    public void notifyUserPostStatus(Post post) {
        log.info("Triggering Notification for User: Post Status Updated");
        
        User author = post.getUser();
        String title = "Cập nhật trạng thái bài viết \uD83D\uDCE2";
        String message;
        String targetUrl = "/posts/" + post.getId(); // Đường dẫn đến chi tiết bài viết

        if (post.getStatus() == PostStatus.APPROVED) {
            message = String.format("Chúc mừng! Bài viết '%s' của bạn đã được duyệt và xuất bản.", post.getTitle());
        } else if (post.getStatus() == PostStatus.REJECTED) {
            message = String.format("Rất tiếc, bài viết '%s' của bạn đã bị từ chối.", post.getTitle());
        } else {
            return; 
        }

        saveAndPush(author, title, message, targetUrl);
    }

    public void notifyUserPostLike(User liker, Post post) {
        // Không gửi thông báo nếu tự like bài của chính mình
        if (liker.getId().equals(post.getUser().getId())) return;

        log.info("Triggering Notification for User: New Post Like");
        
        String title = "Yêu thích mới \u2764\ufe0f";
        String message = String.format("%s đã thích bài viết của bạn: '%s'", 
                liker.getActualUsername(), post.getTitle());
        String targetUrl = "/posts/" + post.getId();

        saveAndPush(post.getUser(), title, message, targetUrl);
    }

    public void notifyNewReview(Review review) {
        log.info("Triggering Notification for New Review on Place: {}", review.getPlace().getName());
        
        Place place = review.getPlace();
        User reviewer = review.getUser();

        // Lấy danh sách những người đã từng review địa điểm này (trừ người vừa viết review)
        List<User> otherReviewers = place.getReviews().stream()
                .map(Review::getUser)
                .filter(u -> !u.getId().equals(reviewer.getId()))
                .distinct()
                .toList();

        String title = "Đánh giá mới tại " + place.getName() + " \u2b50";
        String message = String.format("%s vừa viết một đánh giá cho '%s'. Cùng vào xem nhé!", 
                reviewer.getActualUsername(), place.getName());
        String targetUrl = "/places/" + place.getId();

        for (User user : otherReviewers) {
            saveAndPush(user, title, message, targetUrl);
        }
    }

    /**
     * Kịch bản 1: Welcome Notification
     */
    public void sendWelcomeNotification(User user) {
        log.info("Triggering Welcome Notification for User: {}", user.getEmail());

        String title = "Chào mừng đến với Trip4Hanoi! \uD83C\uDFEE";
        String message = "Chào mừng bạn đến với Trip4Hanoi! 🎉 Hãy vào mục 'Tạo nhanh' để tạo ngay một lịch trình khám phá Hà Nội dành riêng cho bạn.";
        String targetUrl = "/planner";

        saveAndPush(user, title, message, targetUrl);
    }

    private void saveAndPush(User user, String title, String message, String targetUrl, String type) {
        // 1. Lưu vào Database
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .status("UNREAD")
                .build();
        notificationRepository.save(notification);

        // 2. Gửi Push Notification qua FCM
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            fcmService.sendToUser(user, title, message);
        }
    }

    private void saveAndPush(User user, String title, String message, String targetUrl) {
        saveAndPush(user, title, message, targetUrl, "SYSTEM");
    }

    private void saveAndPush(User user, String title, String message) {
        saveAndPush(user, title, message, null, "SYSTEM");
    }

    /**
     * Kịch bản 2: Location-based Notification
     * Được gọi mỗi khi có dữ liệu track vị trí từ User.
     */
    public void processLocationTrigger(User user, Double userLat, Double userLng) {
        log.debug("Processing Location Trigger for User: {} at {}, {}", user.getEmail(), userLat, userLng);

        // Lấy tất cả địa điểm (Trong thực tế nên dùng query giới hạn tọa độ để tối ưu hiệu năng)
        List<Place> allPlaces = placeRepository.findAllByDeletedFalse();

        for (Place place : allPlaces) {
            // Lọc các địa điểm thực sự nổi bật
            if (place.getRatingAvg() != null && place.getRatingAvg() >= MIN_RATING_FOR_SUGGESTION) {
                double distance = calculateDistance(userLat, userLng, place.getLatitude(), place.getLongitude());

                if (distance <= MAX_RADIUS_METERS) {
                    // Logic chống spam
                    log.info("Smart Suggestion: User {} is near highly rated place: {} ({}m)", 
                            user.getEmail(), place.getName(), (int)distance);

                    String title = "Bạn đang ở gần một địa điểm tuyệt vời! \uD83D\uDCCD";
                    String message = String.format("Bạn chỉ cách '%s' khoảng %d mét. Đây là địa điểm được đánh giá %.1f sao. Ghé thăm ngay nhé!", 
                            place.getName(), (int)distance, place.getRatingAvg());
                    String targetUrl = "/places/" + place.getId();

                    saveAndPush(user, title, message, targetUrl);

                    break; 
                }
            }
        }
    }

    /**
     * Kịch bản 3: Weather Alert
     */
    public void processWeatherAlertTrigger(com.trip4hanoi.app.dto.res.WeatherResponse weather) {
        log.debug("Processing Weather Alert Trigger for condition: {}", weather.getCondition());

        boolean isDangerous = weather.getCondition().equalsIgnoreCase("Thunderstorm") 
                || weather.getDescription().contains("mưa rất to")
                || weather.getDescription().contains("dông");

        if (isDangerous) {
            String title = "CẢNH BÁO THỜI TIẾT KHẨN CẤP";
            String message = String.format("Hà Nội đang có %s (%d°C). Bạn hãy tìm nơi trú ẩn an toàn và hạn chế di chuyển ngoài trời nhé!", 
                    weather.getDescription(), (int)weather.getTemp());

            log.warn("Dangerous weather detected! Sending broadcast alerts: {}", message);
            
            List<User> activeUsers = userRepository.findAll().stream()
                    .filter(u -> "ACTIVE".equals(u.getStatus().name()))
                    .toList();

            for (User user : activeUsers) {
                saveAndPush(user, title, message, "/explore");
            }
        }
    }

    /**
     * Thuật toán Haversine tính khoảng cách giữa 2 điểm tọa độ (Đơn vị: Mét)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double longitude2) {
        final int R = 6371; // Bán kính trái đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(longitude2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Đổi ra mét
    }
}
