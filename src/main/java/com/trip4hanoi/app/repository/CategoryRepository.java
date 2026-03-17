package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameIn(List<String> names);
    Optional<Category> findByName(String name);
}
