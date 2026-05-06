package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ album ảnh của một địa điểm.
 */
@Entity
@Table(name = "place_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
}
