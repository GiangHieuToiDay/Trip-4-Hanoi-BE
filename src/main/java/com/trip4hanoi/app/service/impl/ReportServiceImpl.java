package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.ReportRequest;
import com.trip4hanoi.app.dto.res.ReportResponse;
import com.trip4hanoi.app.entity.Notification;
import com.trip4hanoi.app.entity.Report;
import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.NotificationRepository;
import com.trip4hanoi.app.repository.ReportRepository;
import com.trip4hanoi.app.repository.RoleRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.FcmService;
import com.trip4hanoi.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    // Threshold: Báo cáo bao nhiêu lần thì gửi thông báo cho Admin
    private static final int REPORT_THRESHOLD = 3;

    @Override
    @Transactional
    public ReportResponse createReport(Long reporterId, ReportRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reportType(request.getReportType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        report = reportRepository.save(report);

        // Logic Notification thông minh (Cảnh báo hệ thống)
        checkThresholdAndNotifyAdmins(request.getReportType(), request.getTargetId());

        return mapToResponse(report);
    }

    private void checkThresholdAndNotifyAdmins(String reportType, Long targetId) {
        int count = reportRepository.countByReportTypeAndTargetIdAndStatus(reportType, targetId, "PENDING");
        
        // Nếu số lượng báo cáo đạt ngưỡng (vd: 3 lần), gửi cảnh báo cho tất cả Admin
        if (count == REPORT_THRESHOLD) {
            log.info("System Alert: {} with ID {} has reached {} pending reports.", reportType, targetId, REPORT_THRESHOLD);
            
            // Tìm tất cả Admin
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            if (adminRole != null) {
                List<User> admins = userRepository.findByRolesContaining(adminRole);
                String message = String.format("CẢNH BÁO: %s ID %d đã bị báo cáo %d lần. Vui lòng kiểm tra!", reportType, targetId, count);
                
                for (User admin : admins) {
                    //  Lưu vào DB Notification
                    Notification notification = Notification.builder()
                            .user(admin)
                            .message(message)
                            .status("UNREAD")
                            .build();
                    notificationRepository.save(notification);

                    // 2. Gửi Push FCM cho Admin nếu có token
                    fcmService.sendToUser(admin, "Cảnh báo hệ thống \uD83D\uDEA8", message);
                }
            }
        }
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatus("PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportResponse updateReportStatus(Long id, String status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS)); // Dùng tạm mã lỗi
        
        report.setStatus(status);
        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getActualUsername())
                .reportType(report.getReportType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
