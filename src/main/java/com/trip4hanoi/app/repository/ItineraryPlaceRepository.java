package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.ItineraryPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItineraryPlaceRepository extends JpaRepository<ItineraryPlace, Long> {
    List<ItineraryPlace> findByItineraryId(Long itineraryId);
    void deleteByItineraryId(Long itineraryId);
    boolean existsByItineraryIdAndPlaceId(Long itineraryId, Long placeId);
    int countByItineraryIdAndDayNumber(Long itineraryId, Integer dayNumber);
    @Modifying
    @Query("""
    UPDATE ItineraryPlace ip
    SET ip.orderIndex = ip.orderIndex + 1
    WHERE ip.itinerary.id = :itineraryId
      AND ip.dayNumber = :dayNumber
      AND ip.orderIndex >= :orderIndex
""")
    void shiftOrderIndex(Long itineraryId, Integer dayNumber, Integer orderIndex);


    @Modifying
    @Query("""
    UPDATE ItineraryPlace ip
    SET ip.orderIndex = ip.orderIndex - 1
    WHERE ip.itinerary.id = :itineraryId
      AND ip.dayNumber = :day
      AND ip.orderIndex > :orderIndex
""")
    void decreaseOrderIndex(Long itineraryId, int day, int orderIndex);
    @Query("SELECT COALESCE(SUM(ip.estimatedCost), 0) FROM ItineraryPlace ip WHERE ip.itinerary.id = :itineraryId")
    int sumEstimatedCostByItineraryId(Long itineraryId);
}
