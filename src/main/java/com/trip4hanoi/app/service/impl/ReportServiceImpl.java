package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.ReportRequest;
import com.trip4hanoi.app.dto.res.ReportResponse;
import com.trip4hanoi.app.common.PostStatus;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.FcmService;
import com.trip4hanoi.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

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
                String targetName = getTargetTitle(reportType, targetId);
                String message = String.format("CẢNH BÁO: %s '%s' (ID %d) đã bị báo cáo %d lần. Vui lòng kiểm tra!", 
                        reportType, targetName, targetId, count);
                
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
        
        // 1. Cập nhật trạng thái hàng loạt cho cùng một Target
        List<Report> reportsToUpdate = reportRepository.findByReportTypeAndTargetIdAndStatus(
                report.getReportType(), report.getTargetId(), "PENDING");
        
        for (Report r : reportsToUpdate) {
            r.setStatus(status);
        }
        reportRepository.saveAll(reportsToUpdate);

        // 2. Nếu trạng thái là RESOLVED, thực hiện hành động lên nội dung thực tế
        if ("RESOLVED".equals(status)) {
            handleTargetAction(report.getReportType(), report.getTargetId());
        }

        return mapToResponse(report);
    }

    private void handleTargetAction(String reportType, Long targetId) {
        try {
            switch (reportType) {
                case "POST":
                    postRepository.findById(targetId).ifPresent(post -> {
                        post.setStatus(PostStatus.REJECTED);
                        postRepository.save(post);
                        log.info("Admin resolved report: Post ID {} has been hidden/rejected.", targetId);
                    });
                    break;
                case "COMMENT":
                    commentRepository.deleteById(targetId);
                    log.info("Admin resolved report: Comment ID {} has been deleted.", targetId);
                    break;
                case "REVIEW":
                    reviewRepository.deleteById(targetId);
                    log.info("Admin resolved report: Review ID {} has been deleted.", targetId);
                    break;
                case "USER":
                    userRepository.findById(targetId).ifPresent(user -> {
                        // Giả sử có status BANNED hoặc tương tự
                        // user.setStatus("BANNED"); 
                        // userRepository.save(user);
                        log.info("Admin resolved report: User ID {} moderation required.", targetId);
                    });
                    break;
            }
        } catch (Exception e) {
            log.error("Error performing target action for type {} ID {}", reportType, targetId, e);
        }
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterName(report.getReporter().getActualUsername())
                .reportType(report.getReportType())
                .targetId(report.getTargetId())
                .targetTitle(getTargetTitle(report.getReportType(), report.getTargetId()))
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private String getTargetTitle(String reportType, Long targetId) {
        try {
            switch (reportType) {
                case "POST":
                    return postRepository.findById(targetId)
                            .map(Post::getTitle)
                            .orElse("Không tìm thấy bài viết");
                case "COMMENT":
                    return commentRepository.findById(targetId)
                            .map(c -> {
                                String content = c.getContent();
                                return content.length() > 30 ? content.substring(0, 27) + "..." : content;
                            })
                            .orElse("Không tìm thấy bình luận");
                case "REVIEW":
                    return reviewRepository.findById(targetId)
                            .map(r -> {
                                String comment = r.getComment();
                                return comment.length() > 30 ? comment.substring(0, 27) + "..." : comment;
                            })
                            .orElse("Không tìm thấy đánh giá");
                case "USER":
                    return userRepository.findById(targetId)
                            .map(User::getActualUsername)
                            .orElse("Không tìm thấy người dùng");
                default:
                    return "N/A";
            }
        } catch (Exception e) {
            log.error("Error fetching target title for type {} ID {}", reportType, targetId, e);
            return "Lỗi khi lấy thông tin";
        }
    }
}
