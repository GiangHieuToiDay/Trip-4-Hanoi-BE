package com.trip4hanoi.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người gửi report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // Loại báo cáo (POST, COMMENT, USER)
    @Column(nullable = false, length = 50)
    private String reportType; 

    // ID của đối tượng bị báo cáo (Post ID, Comment ID, User ID)
    @Column(nullable = false)
    private Long targetId; 

    // Lý do báo cáo
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    // Trạng thái (PENDING, RESOLVED, DISMISSED)
    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
