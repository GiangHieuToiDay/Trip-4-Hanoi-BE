package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.SavedPlace;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    @Query("SELECT sp FROM SavedPlace sp JOIN FETCH sp.place WHERE sp.user = :user")
    List<SavedPlace> findByUser(@Param("user") User user);
    
    Optional<SavedPlace> findByUserAndPlace(User user, Place place);
    boolean existsByUserAndPlace(User user, Place place);
}
