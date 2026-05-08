package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.service.UserLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class UserLocationController {

    private final UserLocationService userLocationService;

    @PostMapping("/track")
    public ResponseEntity<APIResponse<Void>> trackLocation(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) String district) {

        Long userId = getCurrentUserId();
        if (userId != 0L) {
            userLocationService.saveCurrentLocation(userId, lat, lng, "BACKGROUND", district);
        }

        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Location tracked successfully")
                .build());
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }
}
