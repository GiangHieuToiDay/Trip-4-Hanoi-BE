package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.NotificationRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.NotificationResponse;
import com.trip4hanoi.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j(topic = "NOTIFICATION-CONTROLLER")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<APIResponse<NotificationResponse>> createNotification(@RequestBody NotificationRequest request) {
        log.info("REST request to create notification: {}", request);
        var result = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<NotificationResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Notification created successfully")
                .data(result)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<NotificationResponse>> getNotificationById(@PathVariable Long id) {
        log.info("REST request to get notification by id: {}", id);
        var result = notificationService.getNotificationById(id);
        return ResponseEntity.ok(APIResponse.<NotificationResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(result)
                .build());
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<NotificationResponse>>> getAllNotifications() {
        log.info("REST request to get all notifications");
        var result = notificationService.getAllNotifications();
        return ResponseEntity.ok(APIResponse.<List<NotificationResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<APIResponse<List<NotificationResponse>>> getNotificationsByUserId(@PathVariable Long userId) {
        log.info("REST request to get notifications by user id: {}", userId);
        var result = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(APIResponse.<List<NotificationResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .data(result)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<NotificationResponse>> updateNotification(@PathVariable Long id, @RequestBody NotificationRequest request) {
        log.info("REST request to update notification by id: {}", id);
        var result = notificationService.updateNotification(id, request);
        return ResponseEntity.ok(APIResponse.<NotificationResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Notification updated successfully")
                .data(result)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteNotification(@PathVariable Long id) {
        log.info("REST request to delete notification by id: {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Notification deleted successfully")
                .build());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<APIResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        log.info("REST request to mark notification as read by id: {}", id);
        var result = notificationService.markAsRead(id);
        return ResponseEntity.ok(APIResponse.<NotificationResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Notification marked as read")
                .data(result)
                .build());
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<APIResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        log.info("REST request to mark all notifications as read for user: {}", userId);
        notificationService.markAllAsReadByUserId(userId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("All notifications marked as read")
                .build());
    }


    /**
     * Lưu hoặc update FCM token cho user
     * API: POST /api/notifications/token
     *
     * Dùng để:
     * - frontend/mobile gửi FCM token lên backend
     * - backend lưu token vào DB
     * - phục vụ push notification bằng Firebase
     *
     * Flow:
     * Mobile/Web lấy được token từ Firebase
     *      ↓
     * Gọi API này gửi token lên server
     *      ↓
     * Server lưu token theo user
     *      ↓
     * Sau này backend dùng token đó để push notification
     */
    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<APIResponse<String>> setFcmToken(
            @RequestParam String token,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("id");
        log.info("REST request to set FCM token for user: {}", userId);
        notificationService.updateFcmToken(userId, token);
        return ResponseEntity.ok(APIResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("FCM token updated successfully")
                .data("Token updated")
                .build());
    }

}
