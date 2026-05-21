package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import com.trip4hanoi.app.common.PaymentStatus;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderCode(String orderCode);

    @Query("SELECT p FROM PaymentOrder p WHERE " +
           "(:keyword IS NULL OR LOWER(p.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<PaymentOrder> searchOrdersAdmin(String keyword, PaymentStatus status, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM PaymentOrder p WHERE p.status = 'SUCCESS'")
    Long sumTotalRevenue();

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM PaymentOrder p WHERE p.status = 'SUCCESS'")
    Long countProUsers();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, SUM(amount) as revenue " +
           "FROM payment_orders WHERE status = 'SUCCESS' " +
           "GROUP BY month ORDER BY month DESC", nativeQuery = true)
    List<Object[]> getRevenueGrowthByMonth();
}
