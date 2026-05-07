package com.trip4hanoi.app.service;


import com.trip4hanoi.app.entity.Event;
import org.springframework.stereotype.Service;

@Service
public interface EventReminderService {

    void sendEventReminder();

    void updateEvent(Event event);

}
