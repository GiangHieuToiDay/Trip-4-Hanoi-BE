package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "WHERE :keyword IS NULL " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findAllByRoleId(@Param("roleId") Long roleId);

    User findByVerificationCode(String verificationCode);

    // --- DASHBOARD QUERIES ---
    @Query("SELECT r.name, COUNT(u.id) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUsersByRole();

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u WHERE EXISTS " +
            "(SELECT 1 FROM Itinerary i WHERE i.user.id = u.id) OR " +
            "EXISTS (SELECT 1 FROM Post p WHERE p.user.id = u.id)")
    long countConvertedUsers();

    List<User> findByRolesContaining(Role role);
}
