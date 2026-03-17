package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.ItineraryPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItineraryPlaceRepository extends JpaRepository<ItineraryPlace, Long> {
    List<ItineraryPlace> findByItineraryId(Long itineraryId);
    void deleteByItineraryId(Long itineraryId);
}
