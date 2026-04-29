package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID người dùng

    @Column(nullable = false)
    private String name; // Tên hiển thị

    @Column(nullable = false, unique = true)
    private String email; // Email đăng nhập

    @Column(nullable = false)
    private String password; // Mật khẩu (đã mã hóa)

    @Enumerated(EnumType.STRING)
    private Role role; // Quyền: USER / ADMIN

    @Column(name = "google_id")
    private String googleId; // ID đăng nhập bằng Google

    private String nationality; // Quốc tịch

    private String language; // Ngôn ngữ ưu tiên

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Ngày tạo tài khoản

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPreference> userPreferences; // Danh sách sở thích của user

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Itinerary> itineraries; // Các lịch trình đã tạo

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews; // Các đánh giá đã viết

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserEventFollow> userEventFollows; // Các sự kiện đang theo dõi

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications; // Danh sách thông báo

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts; // Các bài đăng của user

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments; // Các comment của user trên bài đăng

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SavedPlace> savedPlaces; // Các địa điểm đã lưu (bookmark)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostLike> postLikes; // Các lượt like bài viết của user

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostSave> postSaves; // Các bài viết user đã lưu

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
