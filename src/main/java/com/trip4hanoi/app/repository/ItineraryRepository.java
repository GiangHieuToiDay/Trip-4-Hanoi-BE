package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByUserId(Long userId);
    Optional<Itinerary> findByUserIdAndTitleIgnoreCase(Long userId, String title);
    boolean existsByTitleIgnoreCase(String title);
    Itinerary findByTitleIgnoreCase(String title);
    @Query("""
    SELECT i FROM Itinerary i
    LEFT JOIN FETCH i.itineraryPlaces ip
    LEFT JOIN FETCH ip.place p
    WHERE i.id = :id
""")
    Itinerary findByIdWithPlaces(Long id);

}
