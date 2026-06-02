package com.trip4hanoi.app.entity;


import com.trip4hanoi.app.common.PaymentStatus;
import com.trip4hanoi.app.common.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;


    // ID mã đơn hàng PayOS trả về
    @Column(name = "payos_order_code", unique = true)
    private Long payosOrderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;


    // Loại gói cước: 1_MONTH, 3_MONTH
    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false)
    private PlanType packageType;


    @Column(name = "amount" , nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime creationDate;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updateDate;



}
