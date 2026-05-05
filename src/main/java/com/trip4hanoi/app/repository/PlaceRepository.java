package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Category;
import com.trip4hanoi.app.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> , JpaSpecificationExecutor<Place> {
    List<Place> findByCategoryId(Long categoryId);


}
