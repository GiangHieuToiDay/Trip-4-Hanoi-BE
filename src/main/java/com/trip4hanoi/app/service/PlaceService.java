package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import java.util.List;

public interface PlaceService {
    List<PlaceResponse> getAllPlaces(Long categoryId);
    PlaceDetailResponse getPlaceDetail(Long id);
}
