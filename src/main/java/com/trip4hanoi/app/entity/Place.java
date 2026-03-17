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
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "price_avg")
    private Integer priceAvg;

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<Event> events;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<ItineraryPlace> itineraryPlaces;
}
