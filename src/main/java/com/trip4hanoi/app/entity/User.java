package com.trip4hanoi.app.entity;

import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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


    private String nationality; // Quốc tịch

    private String language; // Ngôn ngữ ưu tiên

    private String avatar;

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Ngày tạo tài khoản

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name="status",length = 255)
    private UserStatus status;


    @Column(name="verification_code", length = 255)
    private String verificationCode;

    @Column(name = "fcm_token")
    private String fcmToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles;

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
