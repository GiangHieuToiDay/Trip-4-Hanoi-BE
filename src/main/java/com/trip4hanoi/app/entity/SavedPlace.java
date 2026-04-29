package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity lưu trữ thông tin địa điểm mà người dùng đã bookmark.
 */
@Entity
@Table(name = "saved_places")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
}
