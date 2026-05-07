package com.trip4hanoi.app.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public void send(String token) {

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Test Notification")
                            .setBody("Hello từ Spring Boot ")
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            System.out.println("Sent: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToUser(com.trip4hanoi.app.entity.User user, String title, String body) {
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            System.out.println("User " + user.getEmail() + " has no FCM token");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Sent to " + user.getEmail() + ": " + response);

        } catch (Exception e) {
            System.err.println("Failed to send FCM to " + user.getEmail() + ": " + e.getMessage());
        }
    }
}