package com.trip4hanoi.app.service;

public interface UserLocationService {
    void saveCurrentLocation(Long userId, Double lat, Double lng, String actionType, String district);
    void cleanupOldHistory();
}
