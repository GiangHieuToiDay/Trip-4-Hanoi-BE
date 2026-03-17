package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "itineraries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    private Integer budget;

    private Integer days;

    private Integer numberOfPeople;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL)
    private List<ItineraryPlace> itineraryPlaces;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
