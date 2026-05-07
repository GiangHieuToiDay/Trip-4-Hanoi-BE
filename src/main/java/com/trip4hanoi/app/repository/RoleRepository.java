package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
    java.util.List<Role> findAllByNameIn(java.util.Collection<String> names);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(:keyword) OR LOWER(r.description) LIKE LOWER(:keyword)")
    org.springframework.data.domain.Page<Role> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
