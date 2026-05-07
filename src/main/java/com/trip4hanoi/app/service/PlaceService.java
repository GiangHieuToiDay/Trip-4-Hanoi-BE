package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.PlaceFilterRequest;
import com.trip4hanoi.app.dto.req.PlaceRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlaceService {
    List<PlaceResponse> getAllPlaces(Long categoryId);
    PlaceDetailResponse getPlaceDetail(Long id);
    PlaceResponse createPlace(PlaceRequest request, MultipartFile[] images);
    PlaceResponse updatePlace(Long id, PlaceRequest request, MultipartFile[] images);
    void deletePlace(Long id);
    PageResponse<PlaceResponse> searchPlaces(PlaceFilterRequest request);
    PageResponse<PlaceResponse> getAllPlacesForAdmin(String keyword, Long categoryId, String district, String sort, int page, int size);
}
