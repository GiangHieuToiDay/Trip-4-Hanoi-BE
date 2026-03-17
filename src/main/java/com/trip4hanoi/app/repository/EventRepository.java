package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByPlaceId(Long placeId);
}
