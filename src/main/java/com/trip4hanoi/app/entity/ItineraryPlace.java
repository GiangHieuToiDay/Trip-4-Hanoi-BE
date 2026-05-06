package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_places")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "order_index")
    private Integer orderIndex;

    private String session;

    @Column(name = "estimated_cost")
    private Integer estimatedCost;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "event_id")
   private Event event;

}
