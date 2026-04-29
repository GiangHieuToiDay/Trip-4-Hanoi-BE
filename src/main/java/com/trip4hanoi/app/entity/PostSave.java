package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ bài đăng mà người dùng đã lưu lại.
 */
@Entity
@Table(name = "post_saves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
