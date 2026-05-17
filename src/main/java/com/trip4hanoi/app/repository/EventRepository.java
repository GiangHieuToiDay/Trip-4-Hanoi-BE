package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByPlaceId(Long placeId);

    Optional<Event> findByNameAndDeletedFalse(String name);

    @Query("SELECT e FROM Event e " +
            "WHERE e.deleted = false " +
            "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:placeId IS NULL OR e.place.id = :placeId) " +
            "ORDER BY CASE WHEN LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 0 ELSE 1 END, e.id DESC")
    Page<Event> searchEventsAdmin(@Param("keyword") String keyword, @Param("placeId") Long placeId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.deleted = false " +
            "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:placeId IS NULL OR e.place.id = :placeId) " +
            "AND (e.endTime >= :now) " +
            "ORDER BY CASE WHEN LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 0 ELSE 1 END, e.startTime ASC")
    Page<Event> searchEventsUser(@Param("keyword") String keyword, @Param("placeId") Long placeId, @Param("now") LocalDateTime now, Pageable pageable);

    //=============================================================================================
    //DASHBOARD

    // Thống kê sự kiện hot dựa trên lượng tương tác
    @Query(value = "SELECT e.id, e.name, " +
            "((SELECT COUNT(*) FROM user_event_follows WHERE event_id = e.id) + " +
            "(SELECT COUNT(*) FROM event_subscriptions WHERE event_id = e.id)) as hotness, " +
            "e.start_time, e.end_time " +
            "FROM events e WHERE e.deleted = false ORDER BY hotness DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findTopHotEvents();



}
