package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ ảnh trong một bài đăng.
 */
@Entity
@Table(name = "post_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String imageUrl;

    private String publicId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
