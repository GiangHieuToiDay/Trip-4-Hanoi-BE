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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;


@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Slf4j(topic = "PLACE-CONTROLLER")
public class PlaceController {
   private final PlaceService placeService;


   /**
    * ENDPOINT - USER: Lấy danh sách địa điểm theo category
    * @param categoryId
    * @return
    */
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
    * ENDPOINT - USER: Tìm kiếm địa điểm (Có proximity, keyword, category)
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
    * ENDPOINT - ADMIN: Quản lý địa điểm cho Dashboard (Ưu tiên sắp xếp, Lọc Admin)
    */
   @GetMapping("/admin")
   @PreAuthorize("hasAuthority('MANAGE_PLACE')")
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


   /**
    * ENDPOINT - USER/ADMIN: Lấy chi tiết địa điểm kèm album ảnh
    */
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

   /**
    * ENDPOINT - ADMIN: Tạo địa điểm mới với album ảnh (Multipart)
    */
   @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
   @PreAuthorize("hasAuthority('MANAGE_PLACE')")
   public ResponseEntity<APIResponse<PlaceResponse>> createPlace(
           @RequestPart("data") PlaceRequest request,
           @RequestPart(value = "images", required = false) MultipartFile[] images) {
       PlaceResponse place = placeService.createPlace(request, images);


       APIResponse<PlaceResponse> response = APIResponse.<PlaceResponse>builder()
               .status(HttpStatus.CREATED.value())
               .code(1000)
               .message("Successfully created place")
               .data(place)
               .build();


       return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   /**
    * ENDPOINT - ADMIN: Cập nhật địa điểm và quản lý ảnh
    */
   @PutMapping(value = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
   @PreAuthorize("hasAuthority('MANAGE_PLACE')")
   public ResponseEntity<APIResponse<PlaceResponse>> updatePlace(
           @PathVariable Long id, 
           @RequestPart("data") PlaceRequest request,
           @RequestPart(value = "images", required = false) MultipartFile[] images) {
       PlaceResponse place = placeService.updatePlace(id, request, images);


       APIResponse<PlaceResponse> response = APIResponse.<PlaceResponse>builder()
               .status(HttpStatus.OK.value())
               .code(1000)
               .message("Successfully updated place")
               .data(place)
               .build();


       return ResponseEntity.ok(response);
   }


   @DeleteMapping("/{id}")
   @PreAuthorize("hasAuthority('MANAGE_PLACE')")
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

