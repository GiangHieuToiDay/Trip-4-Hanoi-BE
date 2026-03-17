package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByCategoryId(Long categoryId);
    List<Place> findByCategoryIn(List<Category> categories);
}
