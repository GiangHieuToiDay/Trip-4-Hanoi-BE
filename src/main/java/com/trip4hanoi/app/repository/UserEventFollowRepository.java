package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.UserEventFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserEventFollowRepository extends JpaRepository<UserEventFollow, Long> {
    List<UserEventFollow> findByUserId(Long userId);
    List<UserEventFollow> findByEventId(Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    long countByEventId(Long eventId);
    void deleteByUserIdAndEventId(Long userId, Long eventId);
}
