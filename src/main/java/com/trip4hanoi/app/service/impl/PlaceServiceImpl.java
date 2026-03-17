package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.PlaceMapper;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    @Override
    public List<PlaceResponse> getAllPlaces(Long categoryId) {
        List<Place> places;
        if (categoryId != null) {
            places = placeRepository.findByCategoryId(categoryId);
        } else {
            places = placeRepository.findAll();
        }
        return places.stream()
                .map(placeMapper::toPlaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlaceDetailResponse getPlaceDetail(Long id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)); // Sẽ đổi mã lỗi sau nếu cần
        return placeMapper.toPlaceDetailResponse(place);
    }
}
