package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Subscription;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository  extends JpaRepository<Subscription,Long> {
    Optional<Subscription> findByUser(User user);
    Optional<Subscription> findByUserId(Long userId);
}
