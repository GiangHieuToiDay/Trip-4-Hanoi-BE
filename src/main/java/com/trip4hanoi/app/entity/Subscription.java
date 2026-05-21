package com.trip4hanoi.app.entity;


import com.trip4hanoi.app.common.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;


    // FREE, PRO
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    // ngày bắt đầu gói
    @Column(name = "start_date")
    private LocalDateTime startDate;


    // Ngày kết thúc gói
    @Column(name = "end_date")
    private LocalDateTime endDate;



    // Trạng thái (true nếu đang trong thời hạn)
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;



    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
