package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.entity.UserLocationHistory;
import com.trip4hanoi.app.repository.UserLocationHistoryRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.UserLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-LOCATION-SERVICE")
public class UserLocationServiceImpl implements UserLocationService {

    private final UserLocationHistoryRepository locationHistoryRepository;
    private final UserRepository userRepository;
    private final com.trip4hanoi.app.service.GeocodingService geocodingService;

    @Override
    @Transactional
    public void saveCurrentLocation(Long userId, Double lat, Double lng, String actionType, String district) {
        if (userId == null || userId == 0L || lat == null || lng == null) {
            return;
        }

        // Tự động xác định quận nếu bị null
        String finalDistrict = district;
        if (finalDistrict == null || finalDistrict.trim().isEmpty()) {
            finalDistrict = geocodingService.getDistrictFromCoords(lat, lng);
        }

        String finalDistrictToSave = finalDistrict;
        userRepository.findById(userId).ifPresent(user -> {
            // Kiểm tra quyền riêng tư của User
            if (Boolean.FALSE.equals(user.getIsLocationTrackingEnabled())) {
                log.debug("Location tracking is disabled for user {}", userId);
                return;
            }

            UserLocationHistory history = UserLocationHistory.builder()
                    .user(user)
                    .latitude(lat)
                    .longitude(lng)
                    .actionType(actionType)
                    .district(finalDistrictToSave)
                    .build();
            locationHistoryRepository.save(history);
            log.debug("Saved location history for user {}: {}, {} ({}) in {}", userId, lat, lng, actionType, finalDistrictToSave);
        });
    }

    /**
     * Tự động xóa dữ liệu cũ hơn 15 ngày
     * Chạy vào lúc 00:00 mỗi ngày
     */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldHistory() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(15);
        locationHistoryRepository.deleteOlderThan(expiryDate);
        log.info("Cleaned up user location history older than {}", expiryDate);
    }
}
