package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.UserPreference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    @EntityGraph(attributePaths = {"category"})
    List<UserPreference> findByUserId(Long userId);
}
