package com.trip4hanoi.app.entity;


import com.trip4hanoi.app.common.ChatMessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id" , nullable = false)
    private ChatRoom room;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender; // tin nhắn System thì sender -> null

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    private LocalDateTime timestamp;

    @PrePersist
    protected  void onCreate(){
        timestamp = LocalDateTime.now();
    }



}
