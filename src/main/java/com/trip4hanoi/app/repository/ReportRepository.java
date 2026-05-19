package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(String status);
    List<Report> findByReportTypeAndTargetId(String reportType, Long targetId);
    int countByReportTypeAndTargetIdAndStatus(String reportType, Long targetId, String status);
}
