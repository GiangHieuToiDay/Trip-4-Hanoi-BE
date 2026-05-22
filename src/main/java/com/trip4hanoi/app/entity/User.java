package com.trip4hanoi.app.entity;

import com.trip4hanoi.app.common.AuthProvider;
import com.trip4hanoi.app.common.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID người dùng

    @Column(nullable = false)
    private String username; // Tên hiển thị

    @Column(nullable = false, unique = true)
    private String email; // Email đăng nhập

    @Column(nullable = false)
    private String password; // Mật khẩu (đã mã hóa)
    @Column(columnDefinition = "TEXT")
    private String avatar;

    private String nationality; // Quốc tịch

    private String language; // Ngôn ngữ ưu tiên


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

    @Column(name = "verification_expired_at")
    private LocalDateTime verificationExpiredAt;

    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Builder.Default
    @Column(name = "email_sent_count")
    private Integer emailSentCount = 0;

    @Column(name = "email_sent_date")
    private LocalDate emailSentDate;

    @Builder.Default
    @Column(name = "is_location_tracking_enabled")
    private Boolean isLocationTrackingEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tbl_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if(this.roles != null){

            this.roles.forEach(role ->
            {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

                if(role.getPermissions() != null){
                    role.getPermissions().forEach(permission ->
                            authorities.add(new SimpleGrantedAuthority(permission.getName())));
                }
            });
        }


        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public String getActualUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }
}
