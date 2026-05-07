package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.NotificationRequest;
import com.trip4hanoi.app.dto.res.NotificationResponse;
import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationRequest request);
    NotificationResponse getNotificationById(Long id);
    List<NotificationResponse> getAllNotifications();
    List<NotificationResponse> getNotificationsByUserId(Long userId);
    NotificationResponse updateNotification(Long id, NotificationRequest request);
    void deleteNotification(Long id);
    NotificationResponse markAsRead(Long id);
    void updateFcmToken(Long userId, String token);
}
