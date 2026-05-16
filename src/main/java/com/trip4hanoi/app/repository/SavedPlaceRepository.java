package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.SavedPlace;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findByUser(User user);
    Optional<SavedPlace> findByUserAndPlace(User user, Place place);
    boolean existsByUserAndPlace(User user, Place place);
}
