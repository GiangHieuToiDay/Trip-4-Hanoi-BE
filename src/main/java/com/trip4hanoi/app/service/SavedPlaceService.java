package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.SavedPlaceResponse;
import java.util.List;

public interface SavedPlaceService {
    SavedPlaceResponse toggleSavePlace(Long placeId);
    List<SavedPlaceResponse> getMySavedPlaces();
    boolean isPlaceSaved(Long placeId);
}
