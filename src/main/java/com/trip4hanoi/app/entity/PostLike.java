package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ lượt thích của người dùng trên bài đăng.
 */
@Entity
@Table(name = "post_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike {
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
