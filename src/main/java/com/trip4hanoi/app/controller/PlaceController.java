package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.PlaceFilterRequest;
import com.trip4hanoi.app.dto.req.PlaceRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PlaceDetailResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Slf4j(topic = "PLACE-CONTROLLER")
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping
    public ResponseEntity<APIResponse<List<PlaceResponse>>> getAllPlaces(
            @RequestParam(required = false) Long categoryId) {

        List<PlaceResponse> places = placeService.getAllPlaces(categoryId);

        APIResponse<List<PlaceResponse>> response = APIResponse.<List<PlaceResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved places")
                .data(places)
                .build();

        return ResponseEntity.ok(response);
    }


    /**
     * ENDPOINT CHO USER: Tìm kiếm địa điểm (Có proximity, keyword , category)
     * Sử dụng  @ModelAttribute ĐỂ map toàn bộ query params vào Object PlaceFilterRequest
     * @param request
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<APIResponse<PageResponse<PlaceResponse>>> searchPlaces(
            @ModelAttribute PlaceFilterRequest request
    ){
        log.debug("REST request to search places: {}" ,request);
        PageResponse<PlaceResponse> result = placeService.searchPlaces(request);

        return ResponseEntity.ok(APIResponse.<PageResponse<PlaceResponse>>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully searched places")
                .data(result)
                .build());

    }


    /**
     * ENDPOINT CHO DASHBOARD: Quản lý địa điểm (Ưu tiên sắp xếp , Lọc Admin)
     * @param keyword
     * @param categoryId
     * @param district
     * @param sort
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/admin")
    public ResponseEntity<APIResponse<PageResponse<PlaceResponse>>> getAllPlacesAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "id:desc") String sort,
            @RequestParam(defaultValue = "1") int page ,
            @RequestParam(defaultValue = "10") int size
    ){
        log.info("REST request to get places for Admin dashboard - sort: {}, page: {}", sort, page);
      PageResponse<PlaceResponse> result = placeService.getAllPlacesForAdmin(keyword, categoryId, district, sort, page, size);

      return ResponseEntity.ok(APIResponse.<PageResponse<PlaceResponse>>builder()
                      .status(HttpStatus.OK.value())
                      .code(1000)
                      .message("Successfully retrieved places for admin")
                      .data(result)
              .build());
    }





    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long id) {
        PlaceDetailResponse place = placeService.getPlaceDetail(id);

        APIResponse<PlaceDetailResponse> response = APIResponse.<PlaceDetailResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully retrieved place detail")
                .data(place)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<APIResponse<PlaceResponse>> createPlace(@RequestBody PlaceRequest request) {
        PlaceResponse place = placeService.createPlace(request);

        APIResponse<PlaceResponse> response = APIResponse.<PlaceResponse>builder()
                .status(HttpStatus.CREATED.value())
                .code(1000)
                .message("Successfully created place")
                .data(place)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<PlaceResponse>> updatePlace(@PathVariable Long id, @RequestBody PlaceRequest request) {
        PlaceResponse place = placeService.updatePlace(id, request);

        APIResponse<PlaceResponse> response = APIResponse.<PlaceResponse>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully updated place")
                .data(place)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);

        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .code(1000)
                .message("Successfully deleted place")
                .build();

        return ResponseEntity.ok(response);
    }
}
