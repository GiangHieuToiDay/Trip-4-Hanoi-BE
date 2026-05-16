package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.SavedPlaceResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.SavedPlace;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.SavedPlaceMapper;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.SavedPlaceRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.SavedPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPlaceServiceImpl implements SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final SavedPlaceMapper savedPlaceMapper;

    @Override
    @Transactional
    public SavedPlaceResponse toggleSavePlace(Long placeId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        Optional<SavedPlace> existing = savedPlaceRepository.findByUserAndPlace(user, place);
        
        if (existing.isPresent()) {
            savedPlaceRepository.delete(existing.get());
            // Update favorite count
            place.setFavoriteCount(Math.max(0, place.getFavoriteCount() - 1));
            placeRepository.save(place);
            return null; // Indicates unsaved
        } else {
            SavedPlace savedPlace = SavedPlace.builder()
                    .user(user)
                    .place(place)
                    .build();
            SavedPlace saved = savedPlaceRepository.save(savedPlace);
            // Update favorite count
            place.setFavoriteCount(place.getFavoriteCount() + 1);
            placeRepository.save(place);
            return savedPlaceMapper.toSavedPlaceResponse(saved);
        }
    }

    @Override
    public List<SavedPlaceResponse> getMySavedPlaces() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        return savedPlaceRepository.findByUser(user).stream()
                .map(savedPlaceMapper::toSavedPlaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlaceSaved(Long placeId) {
        Long userId = getCurrentUserId();
        if (userId == 0L) return false;
        
        User user = userRepository.findById(userId).orElse(null);
        Place place = placeRepository.findById(placeId).orElse(null);
        
        if (user == null || place == null) return false;
        
        return savedPlaceRepository.existsByUserAndPlace(user, place);
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object idClaim = jwt.getClaims().get("id");
            if (idClaim instanceof Number n) {
                return n.longValue();
            }
        }
        return 0L;
    }
}
