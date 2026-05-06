package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByPlaceId(Long placeId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.deleted = false " +
            "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:placeId IS NULL OR e.place.id = :placeId)")
    Page<Event> searchEventsAdmin(@Param("keyword") String keyword, @Param("placeId") Long placeId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.deleted = false " +
            "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:placeId IS NULL OR e.place.id = :placeId) " +
            "AND (e.endTime >= :now)")
    Page<Event> searchEventsUser(@Param("keyword") String keyword, @Param("placeId") Long placeId, @Param("now") java.time.LocalDateTime now, Pageable pageable);
}
