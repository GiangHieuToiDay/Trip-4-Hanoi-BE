package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.UserLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserLocationHistoryRepository extends JpaRepository<UserLocationHistory, Long> {
    List<UserLocationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("DELETE FROM UserLocationHistory ulh WHERE ulh.createdAt < :expiryDate")
    void deleteOlderThan(LocalDateTime expiryDate);

    @Query("SELECT ulh.district FROM UserLocationHistory ulh " +
           "WHERE ulh.user.id = :userId AND ulh.createdAt > :since " +
           "GROUP BY ulh.district " +
           "ORDER BY COUNT(ulh.id) DESC")
    List<String> findTopDistricts(Long userId, LocalDateTime since);
}
