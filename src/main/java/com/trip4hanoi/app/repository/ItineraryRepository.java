package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Itinerary;
import com.trip4hanoi.app.entity.ItineraryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Itinerary> findByIsFeaturedTrue();
    List<Itinerary> findByIsSampleTrue();
    List<Itinerary> findByIsSampleTrueAndStatus(ItineraryStatus status);
    Page<Itinerary> findAllByIsSample(Boolean isSample, Pageable pageable);
    Page<Itinerary> findAllByIsSampleAndStatus(Boolean isSample, ItineraryStatus status, Pageable pageable);
    Page<Itinerary> findAllByStatus(ItineraryStatus status, Pageable pageable);

    @Query("""
    SELECT i FROM Itinerary i 
    WHERE (:keyword IS NULL 
           OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(i.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:isSample IS NULL OR i.isSample = :isSample)
    AND (:status IS NULL OR i.status = :status)
""")
    Page<Itinerary> searchItinerariesAdmin(
            @org.springframework.data.repository.query.Param("keyword") String keyword, 
            @org.springframework.data.repository.query.Param("isSample") Boolean isSample, 
            @org.springframework.data.repository.query.Param("status") ItineraryStatus status, 
            Pageable pageable);

    @Query("""
    SELECT i FROM Itinerary i
    LEFT JOIN FETCH i.itineraryPlaces ip
    LEFT JOIN FETCH ip.place p
    WHERE i.id = :id
""")
    Itinerary findByIdWithPlaces(Long id);

}
