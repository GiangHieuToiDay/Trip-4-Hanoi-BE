package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> , JpaSpecificationExecutor<Place> {
    List<Place> findByCategoryIdAndDeletedFalse(Long categoryId);
    @EntityGraph(attributePaths = {"category", "images"})
    List<Place> findAllByDeletedFalse();
    Optional<Place> findByNameAndDeletedFalse(String name);
    Optional<Place> findByName(String name);

    // --- DASHBOARD QUERIES ---
    @Query(value = "SELECT id, name, (favorite_count * 2 + (SELECT COUNT(*) FROM reviews WHERE place_id = p.id)) as score, view_count, rating_avg " +
            "FROM places p WHERE deleted = false ORDER BY score DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findTop10PopularPlaces();

    @Query("SELECT p FROM Place p WHERE p.viewCount = 0 AND p.favoriteCount = 0 AND p.deleted = false")
    List<Place> findAbandonedPlaces();

    @Query("SELECT c.name, AVG(p.ratingAvg) FROM Place p JOIN p.category c GROUP BY c.name")
    List<Object[]> getAverageRatingByCategory();

    @Query("SELECT DISTINCT p.district FROM Place p WHERE p.district IS NOT NULL AND p.deleted = false ORDER BY p.district ASC")
    List<String> findDistinctDistricts();
}
