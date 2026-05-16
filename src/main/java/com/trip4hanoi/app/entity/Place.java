package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID địa điểm

    @Column(nullable = false)
    private String name; // Tên địa điểm

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả địa điểm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Danh mục (Ăn uống, Chill, Lịch sử...)

    private String address; // Địa chỉ chi tiết

    private String district; // Quận/Huyện

    @Column(nullable = false)
    private Double latitude; // Vĩ độ

    @Column(nullable = false)
    private Double longitude; // Kinh độ

    @Column(name = "price_avg")
    private Integer priceAvg; // Giá trung bình

    @Column(name = "rating_avg")
    private Double ratingAvg; // Điểm đánh giá trung bình

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0; // Lượt xem địa điểm

    @Builder.Default
    @Column(name = "favorite_count")
    private Integer favoriteCount = 0; // Lượt yêu thích/lưu địa điểm

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> images; // Album ảnh của địa điểm này

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<Review> reviews; // Các đánh giá của địa điểm này

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<Event> events; // Các sự kiện diễn ra tại đây

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<ItineraryPlace> itineraryPlaces; // Xuất hiện trong các lịch trình

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<SavedPlace> savedByUsers; // Danh sách user đã lưu địa điểm này

    @ManyToMany(mappedBy = "places")
    private List<Post> posts; // Các bài đăng tag địa điểm này

    @Builder.Default
    private boolean deleted = false;

    @Column(name = "search_vector", columnDefinition = "TEXT")
    private String searchVector; // Tên + địa chỉ + danh mục không dấu để search
}
